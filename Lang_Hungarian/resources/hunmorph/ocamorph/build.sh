#!/bin/bash
set -e 
VERSION=1.1
TARGETS="ocamorph.native ocastem.native" 
FLAGS="-no-links -I src/lib -I src/wrappers/ocastem -I src/wrappers/ocamorph"
MORPHDB_LATEST="morphdb-hu-latest.tgz"
OCAMLBUILD=ocamlbuild
OCAMORPH_NATIVE="src/wrappers/ocamorph/ocamorph.native"
clean ()
{

  $OCAMLBUILD -clean;
  
    
} 
ocb() 
{ 
  $OCAMLBUILD $FLAGS $TARGETS 
} 

usage()
{
    echo "usage: ./build [ build | clean | release linux | release win | release macosx ]"
}

copy-release-files ()
{
  cp ../README.ocastem $1
}

make-bin ()
{
  if [ ! -e morphdb-hu-latest.tgz ]; then
      echo "downloading morphdb-hu-latest from the mokk ftp server"
      wget ftp://ftp.mokk.bme.hu/Tool/Hunmorph/Resources/Morphdb.hu/morphdb-hu-latest.tgz ;
      tar xzf morphdb-hu-latest.tgz;
  else
    echo "downloaded morphdb-hu-latest has been found."
    echo "be careful, its timestamp is not checked by this script"
    echo "if in doubt, just erase _build/morphdb-hu-latest.tgz and repeat the build."
  fi
  MORPHDB_BIN=morphdb.hu/morphdb_hu.bin
  if [ ! -e $MORPHDB_BIN ]; then
      echo "building binary resource, this will take a few minutes...";
      echo "ablakot" | $OCAMORPH_NATIVE --aff morphdb.hu/morphdb_hu.aff --dic  morphdb.hu/morphdb_hu.dic --bin $MORPHDB_BIN
  else
      echo "binary resource file has been found."
      echo "be careful, its timestamp is not checked by this script."
      echo "if in doubt, just erase _build/morphdb-hu-latest.tgz and _build/$MORPHDB_BIN, and repeat the build."
  fi
}
release-nx ()
{ 

  cd _build
  
  DIR=ocamorph-$VERSION-$1
  rm -rf $DIR; mkdir $DIR
  if [ $1 == "win" ]; then
    EXT=".exe"
    if [ ! -e /bin/cygwin1.dll ]; then
      echo "cygwin1.dll dynamic library not found."
      echo "it will be not packaged with the release."
    else
      echo "packaging the cygwin1.dll dynamic library."
      cp /bin/cygwin1.dll $DIR
    fi
  fi
  cp src/wrappers/ocamorph/ocamorph.native $DIR/ocamorph$EXT
  cp src/wrappers/ocastem/ocastem.native $DIR/ocastem$EXT
  copy-release-files $DIR
  
  make-bin
  echo "copying the morphdb.hu resources into the release..."
  cp -Rf morphdb.hu $DIR/

  echo "compressing the release..."
  if [ $1 == "win" ]; then
    ARCHIVE=$DIR.zip
    rm -f $ARCHIVE
    zip -r $ARCHIVE $DIR
  else
    ARCHIVE=$DIR.tgz
    rm -f $ARCHIVE
    tar cvfz $ARCHIVE $DIR
  fi

  echo
  echo "the release is ready at _build/$ARCHIVE"
  echo

  echo "uploading the release to its proper place at the mokk ftp server..."
  echo "(obviously, this final step will fail if you are not the maintainer."
  echo "in this case, just press Ctrl-C and enjoy the release.)"
  echo
  echo -n "please enter your kruso.mokk.bme.hu username: "
  read USERNAME
  scp $ARCHIVE $USERNAME@kruso.mokk.bme.hu:/public/Tool/Hunmorph/Runtime/Ocamorph/Pre/
  ssh $USERNAME@kruso.mokk.bme.hu chmod -w /public/Tool/Hunmorph/Runtime/Ocamorph/Pre/$ARCHIVE
}
release()
{
  if [ $# -eq 0 ]; then 
    echo "please specify a platform";
    usage
  else
    case $1 in
    linux) ;;
    macosx) ;;
    win) ;;
    *) echo "unknown platform $1" ; exit ;;
    esac;

    ocb;
    release-nx $1;
  fi
}


rule() { 

  case $1 in 
   clean) clean;; 
   
   build) ocb  ;;
   
   release) shift; release $*;; 
   
  
   
   *) echo "unknown action $1";; 

   esac; 
} 

if [ $# -eq 0 ]; then 
  usage ;
else 
  rule $*; 
fi 
