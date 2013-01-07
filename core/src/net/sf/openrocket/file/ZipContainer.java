/**
 * 
 */
package net.sf.openrocket.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;


/**
 * Represents a zip container file
 * Data can be written to the file and files attached, the corresponding zip file will be written when save is called
 * @author Richard Graham  
 */
public class ZipContainer {
	private static final LogHelper log = Application.getLogger();

	private StringBuilder xml = new StringBuilder(); 
	private Map<String, byte[]> files = new HashMap<String, byte[]>();
	
	OutputStream output;
	
	public ZipContainer(OutputStream output){
		log.info("Starting new .ork container file");
		this.output = output;
	}
	
	public void write(String s){
		xml.append(s);
	}
	
	public void addFile(String filename, byte[] bytes){
		files.put(filename, bytes);
	}
	
	public boolean hasFile(String filename){
		return files.containsKey(filename);
	}
	
	public void save() throws IOException{
		
		log.info("Saving .ork container file");

		ZipOutputStream zos = new ZipOutputStream(output);
		zos.setLevel(9);
		
		// Save the XML data
		zos.putNextEntry( new ZipEntry("rocket.ork") );
		zos.write(xml.toString().getBytes());
		zos.closeEntry();
		
		// Save other files inside zip
		for (Map.Entry<String, byte[]> file : files.entrySet()) {
		    String filename = file.getKey();
		    byte[] bytes = file.getValue();
		    zos.putNextEntry( new ZipEntry(filename));
		    zos.write(bytes);
		}
		
		log.debug("Assembly of ork file complete, flushing buffers");
		zos.close();
		output.flush();
	}
}
