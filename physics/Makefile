all: main.tex
	mkdir -p compiled
	rubber --pdf --warn all --unsafe --into compiled main.tex

clean:
	rm -rf compiled

open:
	open compiled/main.pdf
