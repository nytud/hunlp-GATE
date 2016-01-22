(*
 * Ocamorph word analysis 
 * high-level wrapper around the analysis engine
 * Copyright (C) 2005 Viktor TRON
 * 
 *)

(*i $Id: ocamorph.ml,v 1.1.1.1 2009-08-04 00:11:16 root Exp $ i*)

let _ =
(* DEFAULT SETTINGS *) (* descriptions are below within speclist *)
let     aff_file  = ref "" in
let     dic_file  = ref "" in
let     bin_file  = ref "" in
(* input options *)
let     anal_in_file = ref "-" in
let     field = ref 0 in (* field 0 means whole line a la awk *)
(* output options *)
let     count_analyses = ref false in
let     segmentation = ref false in
let     anal_out_file = ref "-" in
(* these wierd default is for hunspell compatibility *)
let     tag_preamble = ref "> " in
let     tag_sep = ref "\n" in
(* algorithmic options *)
let     no_caps = ref false in
let     minimize = ref false in
let     stop_at_first = ref false in 
let     blocking = ref false in
let     compounds = ref false in
let     guess = ref Analysis.NoGuess in
(* see also general options *)

let usage  =
  "Usage: ocamorph <options>\nOptions:\n   option\tdescription (default settings)\n------------------------------------------"
in

let help_ref = ref false in
let version_ref = ref false in
 
let others string = 
  Utils.carp 0 "Oops: options require a leading '--'. Watch '%s'.\n" string;
  help_ref := true
in

(* the specification of command-line arguments *)
let speclist = [

  "--aff", Arg.String ((:=) aff_file), "\tinput affix file ()";
  "--dic", Arg.String ((:=) dic_file), "\tinput dictionary file ()";
  "--bin", Arg.String ((:=) bin_file), "\tbinary format (no)\n\nALGORITHMIC OPTIONS";

  "--no_caps", Arg.Set no_caps, "\tno capitalization flexibility (no=there is)";
  "--minimize", Arg.Set minimize, "\tminimize the trie [gives better performance, saves space, but takes long to build] (no)";
  "--saf", Arg.Set stop_at_first, "\tstop at first analysis (no)";
  "--compounds", Arg.Set compounds, "\tallow compounds (no)";
  "--guess", Arg.String 
    (function "No" -> guess := Analysis.NoGuess | "Fallback" -> guess := Analysis.Fallback | "Global" -> guess := Analysis.Global | _ -> help_ref := true), "\tguessing mode No|Fallback|Global (No)";
  "--blocking", Arg.Set blocking, "\tblocking by lexicalized relative stems (no)\n\nINPUT OPTIONS";

  "--in", Arg.String ((:=) anal_in_file), "\tinput from file (stdin)";
  "--out", Arg.String ((:=) anal_out_file), "\toutput to file (stdout)";
  "--field", Arg.Int ((:=) field), "\tanalyse only this field (0 = whole line)\n\nOUTPUT OPTIONS";

  "--count_analyses", Arg.Set count_analyses, "\toutputs the number of analyses (no)";
  "--segmentation", Arg.Set segmentation, "\toutputs chunks for analyses (no)";
  "--tag_preamble", Arg.String ((:=) tag_preamble), "\tpreamble string (\"> \")";
  "--tag_sep", Arg.String ((:=) tag_sep), "\ttag separator (newline)\n\nGENERAL OPTIONS";

  "--debug_level", Arg.Int ((:=) Utils.debug_level), "\tdebug level (0)";
  "--help", Arg.Set help_ref, "\tdisplay this list of options and quits";
  "--version", Arg.Set version_ref, "\tdisplays version info and quits";
]
in
Arg.parse speclist others usage;
if !help_ref then ( Arg.usage speclist usage; exit 0 );
if !version_ref then ( Utils.carp 0 "0.1\n"; exit 0 );

let hanal = 
  if !dic_file = "" then (
    if !bin_file = "" then (
      Utils.carp 0 "No resources given.\nSupply a resource in\n(i) ocamorph native format with --bin\nOR\n(ii) dic and aff files with --dic --aff\n";
      raise (Failure "No resources given")
     );
    Analysis.make_marshal 
      !bin_file !no_caps
   ) 
  else
    Analysis.make
      !aff_file (* the affix file should be ommittable *)
      !dic_file 
      (if !bin_file = "" then None else Some !bin_file)
      !minimize
      !no_caps
in

(* the unknown tag changed and is empty string if we count analyses *)
let unknown_tag = if !count_analyses then "" else "UNKNOWN" in

(* this gives a function 
   stop_at_first -> guess -> blocking -> compounds -> 
   analyzer

   type analyzer = Stemmer of (string -> Tag.t) | Analyzer of (string -> Tag.t list) 
 *)

let segment_delim = " " in
let tag_delim_left = "{" in
let tag_delim_right = "}" in
let fallback_to_word_stemming = ref true in
let hanal = 
  let tag_print = if !segmentation then 
      Tag.print_segmentation segment_delim tag_delim_left tag_delim_right 
    else if !Utils.debug_level > 1 then
      (* let tag_segment = Tag.print_segmentation segment_delim tag_delim_left tag_delim_right in  *)
      fun s t -> Tag.print_debug t
    else
      fun s t -> Tag.print t
  in
  if !stop_at_first 
  then 
    match hanal true !blocking !compounds !guess  with 
    | Analysis.Stemmer hanal -> 
	if !fallback_to_word_stemming 
	then  
	  let hanal string = 
	    match hanal string with 
	      Some tag -> 1, tag_print string tag
	    | None -> 0, string
	  in
	  hanal
	else
	  let hanal string = 
	    match hanal string with 
	      Some tag -> 1, tag_print string tag
	    | None -> 0, unknown_tag
	  in
	  hanal
    | _ -> raise (Failure "oops: expected a stemmer func\n")
  else 
    match hanal false !blocking !compounds !guess with 
    | Analysis.Analyzer hanal -> 
	let stringify string = 
	  let tag_print_string t = tag_print string t in
	  let f a = String.concat !tag_sep (List.rev_map (tag_print_string) a) 
	  in f
	in
	let hanal string = 
	  
	  match hanal string with 
	    0, _ -> 0, unknown_tag
	  | no_of_analyses, analyses -> 
	      let stringify = stringify string in 
	      no_of_analyses, stringify analyses 
	in
	hanal
    | _ -> raise (Failure "oops: expected a analyzer func\n")
in

(* this gives a function 
   stop_at_first -> guess -> blocking -> compounds -> 
   analyzer

   type analyzer = Stemmer of (string -> Tag.t) | Analyzer of (string -> Tag.t list) 
 *)

let anal_in_channel = Utils.open_in_channel !anal_in_file in
let anal_out_channel = Utils.open_out_channel !anal_out_file in 

let cut field line = 
  let len = String.length line in
  let rec cut field pos = 
    let new_pos = 
      try 
	String.index_from line pos '\t'
      with 
      | Not_found -> 
	len
      | Invalid_argument _ ->
	  Printf.eprintf "ERROR: not enough fields. Input line follows\n%s\n" line;
	  raise (Failure "not enough fields")
    in
    let span = new_pos - pos in
    if field = 1 then 
      String.sub line pos span
    else
      cut (field - 1) (new_pos + 1)
  in
  cut field 0
in

let input_token = 
  if !field = 0 then 
    function anal_in_channel ->
      let line = input_line anal_in_channel in
      line, String.copy line
  else 
    function anal_in_channel ->
      let line = input_line anal_in_channel in
      let string = cut !field line in
      line, string
in

let hanal = 
  if !count_analyses then 
    let hanal ()  = 
      let line, string = input_token anal_in_channel in
      let no_of_analyses, result_string = hanal string in
      if no_of_analyses = 0 then 
	Printf.fprintf anal_out_channel 
	  "%s%s%s0\n" 
	  !tag_preamble line !tag_sep
      else
	Printf.fprintf anal_out_channel 
	  "%s%s%s%d%s%s\n" 
	  !tag_preamble line !tag_sep no_of_analyses !tag_sep result_string
	  ;
      flush anal_out_channel
    in
    hanal
  else
    let hanal ()  = 
      let line, string = input_token anal_in_channel in
      let no_of_analyses, result_string = hanal string in
      Printf.fprintf anal_out_channel 
	"%s%s%s%s\n" !tag_preamble line !tag_sep
	result_string;
      flush anal_out_channel
    in
    hanal
in

let analyze () = 
  try 
    while true do
      hanal ()
    done
  with End_of_file ->
    flush anal_out_channel
in

let _ = analyze () in
()
  







