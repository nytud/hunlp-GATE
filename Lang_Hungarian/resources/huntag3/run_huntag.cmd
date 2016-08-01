@ECHO OFF

SETLOCAL EnableDelayedExpansion

IF "%1" == "NER" (
  SET MDIR=%CD%/models/NER-szeged-KR-model
  SET CONFIG=!MDIR!/ner_hun_best.yaml
  SET MODEL=!MDIR!/NER-szeged-KR

) ELSE IF "%1" == "NP" (
  SET MDIR=%CD%/models/NP-szeged-msd-model
  SET CONFIG=!MDIR!/hunchunk.hunMIGE_simple.yaml
  SET MODEL=!MDIR!/NP-szeged-msd

) ELSE IF "%1" == "NER_hfst" (
  SET MDIR=%CD%/models/NER-szeged-hfst-model
  SET CONFIG=!MDIR!/ner.szeged.hfst.yaml
  SET MODEL=!MDIR!/NER-szeged-hfst

) ELSE IF "%1" == "NP_hfst" (
  SET MDIR=%CD%/models/maxNP-szeged-hfst-model
  SET CONFIG=!MDIR!/maxnp.szeged.hfst.yaml
  SET MODEL=!MDIR!/maxNP-szeged-hfst

)

IF EXIST "%CD%\..\common\python\python.exe" (
	SET PY="%CD%\..\common\python\python.exe"
) ELSE (
	SET PY="python.exe"
)

%PY% %CD%\huntag.py tag --model=%MODEL% --config-file=%CONFIG%
