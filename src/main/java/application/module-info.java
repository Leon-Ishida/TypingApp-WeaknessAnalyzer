module TypingApp {
	requires javafx.controls;
	requires javafx.fxml;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

	opens application to javafx.fxml, com.fasterxml.jackson.databind, javafx.graphics;
	opens application.home to javafx.fxml;
	opens application.loading to javafx.fxml;
	opens application.typingtest to javafx.fxml;
	opens application.result to javafx.fxml;
	opens application.common to javafx.fxml;
	opens application.practice to javafx.fxml;
	opens application.model to com.fasterxml.jackson.databind;
}