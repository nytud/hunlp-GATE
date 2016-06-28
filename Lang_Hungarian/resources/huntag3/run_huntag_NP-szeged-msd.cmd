@ECHO OFF

SET MDIR=%CD%/models/NP-szeged-msd-model
SET CONFIG=%MDIR%/hunchunk.hunMIGE_simple.yaml
SET MODEL=%MDIR%/NP-szeged-msd

IF EXIST "%CD%\..\common\python\python" (
	SET PY="%CD%\..\common\python\python"
) ELSE (
	SET PY="python"
)

%PY% %CD%\huntag.py tag --model=%MODEL% --config-file=%CONFIG% < %1
