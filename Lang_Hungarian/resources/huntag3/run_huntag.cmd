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
)

IF EXIST "%CD%\..\common\python\python.exe" (
	SET PY="%CD%\..\common\python\python.exe"
) ELSE (
	SET PY="python.exe"
)

%PY% %CD%\huntag.py tag --model=%MODEL% --config-file=%CONFIG%
