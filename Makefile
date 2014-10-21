CQLSH := $(shell which cqlsh 2>/dev/null)
PIP=bin/pip
PYTHON=bin/python
VENV := $(shell which virtualenv 2>/dev/null)
LEIN = resources/lein


analyze:
	${PYTHON} resources/analyze.py

deps: clj-deps python-deps

db:
	${CQLSH} <resources/db.cql

clj-deps:
	${LEIN} deps

python-deps:
	${PIP} install -r requirements.txt

venv:
ifndef VENV
	$(virtualenv not installed. No Python dependecies will be available.)
else
	@$(VENV) -q . >/dev/null
endif
