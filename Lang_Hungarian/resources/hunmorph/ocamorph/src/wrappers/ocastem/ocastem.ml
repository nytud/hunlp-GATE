(* post-hoc filter for ocamorph
   compile: ocamlc -o ocafilter ocafilter.ml 
*)



(* Split a string into a list of substrings based on a delimiter character *)
let split c str = 
  let rec aux s acc = 
    try  let ind=String.index s c in
         aux (String.sub s (ind+1) ((String.length s) - ind -1 )) 
              ((String.sub s 0 ind)::acc)       
    with Not_found -> List.rev (s::acc) 
  in aux str []

;;


(* erre szuksegunk lesz: egy listabol kiszuri az ugyanolyan elemeket. Rendezi a listat, majd
    vegigmegy az elemeken es atmasolja egy uj listaba az unikusakat *)
let unique l =
    let rec aux last output input  = match input with
        [] -> output
      | head :: tail -> if last = head then (aux last output tail ) else (aux head (head::output) tail)
    in
    match (List.sort compare l) with
        [] -> l
    |   head::tail -> aux head (head::[]) tail 

;;
	



(* This is the post-hoc filter to emulate the missing compounds=Fallback function
   of ocamorph. It there is analysis without compounding and guessing return only those.
   The second rule may be redundant if you call ocamorph with Guessing=Fallback option:
   include guessed analysis only there is no other.

  Visszaadja, hogy ismeretlen volt-e a szo, azaz a guessing mukodott-e.
*)
let block_anals anals = 
	let isnotguessed  a = not (String.contains a '?') in
	let isbasic a = not (String.contains a '+' || String.contains a '?') in
	if List.exists isbasic anals  then (false, List.filter isbasic anals)  else
	if List.exists isnotguessed anals   then (false, List.filter isnotguessed anals)  else (true, anals)
	
;;	

(** Egy elemzesrol eltavolit minden derivaciot es inflexiot. Azaz az elso
	/ vagy ? jel utani reszt es szetszedi az osszetettszavaknal. 
	Feltetelezi, hogy a lemma utan ? vagy / van.
	*)
let parse_anal anal =

	let strip_annot a = 
		let ix = try String.index a '?' with  Not_found -> try String.index a '/' with Not_found -> -1 in
		if(ix > 0) then
			String.sub a 0 ix
		else
		a
	in
	(List.map strip_annot (split '+' anal))
;;



let stem (hanal, lowercase, decompounding, guessing_on, oov_filter, known_filter) word =
	let (_, anals) = hanal (String.copy word) in

        (* ez a segmentation miatt kell, de nem tudom, h micsoda *)
	let anals = List.rev_map (Tag.print) anals in		

	(* ocamorph neha duplumokat ad vissza*)
	let anals = unique anals in 
	
	let anals = if lowercase then (List.map (String.lowercase) anals) else anals in
	let (guessed, blocked_anals) = block_anals anals in

	let lemmas =
        if (List.length blocked_anals = 0) || guessed && (not guessing_on) then 
			(* ha guessed, de nem kertek guessinget, akkor a szo maga a lemma *)
			let normalized = if lowercase then (String.lowercase word) else word in
			(normalized::[]) :: []
		else let lemmas = List.map (parse_anal) blocked_anals in
		
		 match guessed with
			true -> oov_filter lemmas
		  | false -> known_filter lemmas
	in
	if decompounding then
		(* az osszetettszo komponenseket kulon stemnek vesszuk *)
		unique (List.flatten  lemmas)
	else
		(* osszetettszavakat osszerakjuk *)
		unique (List.map (String.concat "") lemmas)
;;
		

type heur = All | ShortestLemma | LongestLemma
	
let filter heur (lemmas: string list list) = 
	
	(* rendezi a lemmakat hosszuk szerint. Ha up == true akkor novekvo, egyebkent csokkeno sorrendbe
		a lemmak mar komponensek bontott string list-ek it *)
	let sort_lemmas up (lemmas: string list list) =
		(* string list kiteritett karakterhosszat adja meg *)
		let rec str_len n slits = match slits with
			[] -> n
			| h :: [] -> (n + String.length h)
			| h :: t -> str_len (n + String.length h) t
		in
		let comp l1 l2 =
		    let (len1, len2) = ((str_len 0 l1) , (str_len 0 l2) ) in
			if up then compare len1 len2 else compare len2 len1
			in
		List.sort comp lemmas
	in

	match heur with
		| All -> lemmas
		| ShortestLemma -> (List.hd (sort_lemmas true lemmas)) :: []
		| LongestLemma -> (List.hd (sort_lemmas false lemmas)) :: []
			

;;



(***********************************
	main
	********************************)
let _ =
let help_ref = ref false in
let decompounding_ref = ref true in
let lowercase_ref = ref true in
let heur_known = ref "longest" in
let heur_oov = ref "shortest" in
let ocablocking_ref = ref true in
let ocafirst_ref = ref false in
let guessing_ref = ref true in
let error = ref "" in	
let bin_file = ref "" in 
let no_caps = ref false in 

let usage  =
	 "Usage: ocastem --bin resource_file [options] \nReads words from the standard input and prints them to the stdout with their stems separated by TAB.\n"
in
	
let others s = 
	 Printf.eprintf "Oops: options require a leading '--'. Watch '%s'.\n" s;
	  help_ref := true
in
let set_binary_arg refered s = match s with
	"yes" -> refered := true
	| "no" -> refered := false
	| _ -> error := "use `yes' or `no' for boolean arguments"; help_ref := true
in

let speclist = 
Arg.align [
	 "--bin",   Arg.Set_string bin_file, " Binary resource file";
    "--no_caps", Arg.Set no_caps, "\tno capitalization flexibility (no=there is)";     
    "--ocamorph-blocking",   Arg.String (set_binary_arg ocablocking_ref)   , " Enable blocking at the level of ocamorph `yes' | `no' (default = yes)" ;
     "--ocamorph-firststem",   Arg.String (set_binary_arg ocafirst_ref)   , " Let ocamorph stop at the first stem `yes' | `no' (default = no)" ;
     "--lowercase", Arg.String (set_binary_arg lowercase_ref) , " Convert all characters to lower case `yes' | `no' (default = yes)" ;
	 "--decompounding",   Arg.String (set_binary_arg decompounding_ref)   , " Enable decoumpounding `yes' | `no' (default = yes)" ;
     "--guessing",   Arg.String (set_binary_arg guessing_ref)   , " Enable guessing `yes' | `no' (default = yes)" ;
	 "--stem-known", Arg.String ((:=) heur_known), " Heuristic used to choose the best stem of known words `shortest' | `longest' |  `all'  (default = longest)\n";	 
     "--stem-oov", Arg.String ((:=) heur_oov), " Heuristic used to choose the best stem of OOV words `shortest'  |  `all'  (default = shortest)\n";	 
]
in 
Arg.parse speclist others usage;

if !bin_file = "" then ( error := "No language resource, use --bin argument\n"; help_ref:=true) ;
if !help_ref then ( Printf.eprintf "%s\n" !error ; Arg.usage speclist usage; exit 0 );




let oov_filter = match !heur_oov with
	| "all"      -> filter  All
	| "shortest" -> filter ShortestLemma
	| "longest"  -> raise (Invalid_argument "hey, using --stem_oov longest means --guessing no!")
	| _	         -> raise (Invalid_argument "Invalid --stem_oov option!")
in

let known_filter = match !heur_known with
	| "all"      -> filter  All
	| "shortest" -> filter ShortestLemma
	| "longest"  -> filter LongestLemma
	| _	         -> raise (Invalid_argument "Invalid --stem_known option!")
in

let hanal =
	(* / stop_at_first    0 1 for indexing
	// blocking         0 1
	// compounds        0 1
	// guess            0 1 2 *)
	let a = 
	Analysis.make_marshal 
      !bin_file !no_caps !ocafirst_ref !ocablocking_ref true Analysis.Fallback in
	match a with
		Analysis.Analyzer (f)-> f
  		| Analysis.Stemmer(f)-> (fun w -> match f w with
									Some(r) -> (1,r::[])
									| None -> (0,[]))
		|_ -> failwith ("Mi vaan?")
in			
(* (hanal, lowercase, decompounding, oov_filter, known_filter) *)
let stem = stem (hanal, !lowercase_ref, !decompounding_ref, !guessing_ref, oov_filter, known_filter) in
let rec loop () =
	let word =  (input_line stdin) in
	print_string word;
	List.iter (fun s -> print_char '\t'; print_string s) (stem word);
	print_newline ();
	loop()
in
try loop () with End_of_file -> ()
