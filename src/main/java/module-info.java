module noitautil {
	requires kotlin.stdlib;
	requires java.desktop;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.kotlin;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires com.fasterxml.jackson.databind;
	requires org.slf4j;
	requires org.slf4j.simple;

	exports com.dimdarkevil.noitautil;
	exports com.dimdarkevil.noitautil.model;
}