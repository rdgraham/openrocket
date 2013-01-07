package net.sf.openrocket.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * Allows files stored in a container file read by an input stream to be obtained in various formats 
 * @author rdg
 */

public class ContainerResource {

	private ByteArrayOutputStream baos = null;
	private static final LogHelper log = Application.getLogger();
	
	public ContainerResource( ZipFile file, String identifier) throws IOException {
		
		InputStream stream = null;
		try {
			ZipEntry entry = file.getEntry(identifier);
			stream = file.getInputStream(entry);
	        
		    baos = new ByteArrayOutputStream();
		    byte[] buffer = new byte[1024];
		    int length = 0;
		    while ((length = stream.read(buffer)) != -1) {
		        baos.write(buffer, 0, length);
		    }
		}
		catch (NullPointerException e) {
			log.user("Identifier "+identifier+" not found in container "+file.getName());
			throw new IOException();
		}
		finally {
			if (stream != null) { stream.close(); };
		}
		
        if (baos == null) {
        	throw new IOException();
        }
	}
	
	public byte[] getBytes(){
		return baos.toByteArray();
	}
	
	public List<Double> getDblArray1D(){
		List<Double> data = new ArrayList<Double>();
		
		// This is really slow
		/*
		Scanner scanner;
		try {
			scanner = new Scanner(baos.toString("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			return data;
		}
		
		while (scanner.hasNextDouble()) {
			double val = Double.parseDouble(scanner.next());
			data.add(val);
		}
		
		scanner.close();
		*/
		
		String dataString = null;
		try {
			dataString = baos.toString("US-ASCII");
		}
		catch (UnsupportedEncodingException e) {
			log.error("BUG -- unable to decode data in resource container");
		}
		
		StringTokenizer stringTokenizer = new StringTokenizer(dataString);
		while( stringTokenizer.hasMoreTokens()) {
		    double value = Double.parseDouble( stringTokenizer.nextToken() );
		    data.add(value);
		} 		
	
		return data;
	}
}
