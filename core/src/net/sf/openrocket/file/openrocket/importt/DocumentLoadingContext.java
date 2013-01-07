package net.sf.openrocket.file.openrocket.importt;

import java.util.zip.ZipFile;
import net.sf.openrocket.file.MotorFinder;

public class DocumentLoadingContext {
	
	private int fileVersion;
	private MotorFinder motorFinder;
	private ZipFile container;
	
	public int getFileVersion() {
		return fileVersion;
	}
	
	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}
	
	public MotorFinder getMotorFinder() {
		return motorFinder;
	}
	
	public void setMotorFinder(MotorFinder motorFinder) {
		this.motorFinder = motorFinder;
	}
	
	public void setContainer(ZipFile container) {
		this.container = container;
	}
	
	public ZipFile getContainer(){
		return container;
	}
	
}
