package com.innerdot.azure.rest.example;

import mjson.Json;

public class ARMTemplate {
	private Json template;
	private Json parameters;

	public ARMTemplate(String template) {
		this.template = Json.read(template);
	}
	
	public ARMTemplate(Json template) {
		this.template = template;
	}
	
	public static ARMTemplate fromUri(String uri) {
		Json template = JsonUtils.jsonFromUri(uri);
		if(template != null) {
			return new ARMTemplate(template);
		} else {
			return null;
		}
	}
	
	public static ARMTemplate fromFile(String path) {
		Json template = JsonUtils.jsonFromFile(path);
		if(template != null) {
			return new ARMTemplate(template);
		} else {
			return null;
		}
	}
	
	public void addParameters(Json parameters) {
		this.parameters = parameters;
	}
	
	public String toString() {
		Json r = Json.object().set("properties", Json.object());
		r.at("properties").set("template", this.template);
		System.out.println(r.toString());
		if(this.parameters != null) {
			r.at("properties").set("parameters", this.parameters);
		}
		r.at("properties").set("mode", "Incremental");
		return r.toString();
	}
}