package vmazurok.study.models;

import java.util.List;

public class PackageConfiguration {
	private String name;
	private List<String> images;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getImages() {
		return images;
	}
	public void setImages(List<String> images) {
		this.images = images;
	}
}
