@ECHO OFF

SET MDIR=%CD%/models/NER-szeged-KR-model
SET CONFIG=%MDIR%/ner_hun_best.yaml
SET MODEL=%MDIR%/NER-szeged-KR

IF EXIST "%CD%\..\common\python\python" (
	SET PY="%CD%\..\common\python\python"
) ELSE (
	SET PY="python"
)

%PY% %CD%\huntag.py tag --model=%MODEL% --config-file=%CONFIG% < %1
