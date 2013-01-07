package net.sf.openrocket.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.openrocket.importt.OpenRocketLoader;
import net.sf.openrocket.file.rocksim.importt.RocksimLoader;
import net.sf.openrocket.util.ArrayUtils;
import net.sf.openrocket.util.TextUtil;


/**
 * A rocket loader that auto-detects the document type and uses the appropriate
 * loading.  Supports loading of GZIPed files as well with transparent
 * uncompression.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GeneralRocketLoader extends AbstractRocketLoader {
	
	private static final int READ_BYTES = 300;
	
	private static final byte[] GZIP_SIGNATURE = { 31, -117 }; // 0x1f, 0x8b
	private static final byte[] ZIP_SIGNATURE = TextUtil.convertStringToBytes("PK",Charset.forName("US-ASCII"));
	private static final byte[] OPENROCKET_SIGNATURE = TextUtil.convertStringToBytes("<openrocket",Charset.forName("US-ASCII"));
	private static final byte[] ROCKSIM_SIGNATURE = TextUtil.convertStringToBytes("<RockSimDoc",Charset.forName("US-ASCII"));
	
	private final OpenRocketLoader openRocketLoader = new OpenRocketLoader();
	
	private final RocksimLoader rocksimLoader = new RocksimLoader();
	
	@Override
	protected OpenRocketDocument loadFromStream(InputStream source, ZipFile container, MotorFinder motorFinder) throws IOException,
			RocketLoadException {
		
		// Check for mark() support
		if (!source.markSupported()) {
			source = new BufferedInputStream(source);
		}
		
		// Read using mark()
		byte[] buffer = new byte[READ_BYTES];
		int count;
		source.mark(READ_BYTES + 10);
		count = source.read(buffer);
		source.reset();
		
		if (count < 10) {
			throw new RocketLoadException("Unsupported or corrupt file.");
		}
		
		// Detect the appropriate loader
		
		// Check for GZIP
		if (buffer[0] == GZIP_SIGNATURE[0] && buffer[1] == GZIP_SIGNATURE[1]) {
			OpenRocketDocument doc = loadFromStream(new GZIPInputStream(source), null, motorFinder);
			return doc;
		}
		
		// Check for ZIP container
		if (buffer[0] == ZIP_SIGNATURE[0] && buffer[1] == ZIP_SIGNATURE[1]) {
			// Search for entry with name *.ork
			ZipInputStream in = new ZipInputStream(source);
			while (true) {
				ZipEntry entry = in.getNextEntry();
				if (entry == null) {
					throw new RocketLoadException("Unsupported or corrupt file.");
				}
				if (entry.getName().matches(".*\\.[oO][rR][kK]$")) {
					OpenRocketDocument doc = loadFromStream(in, container, motorFinder);
					in.close();
					return doc;
				} else if ( entry.getName().matches(".*\\.[rR][kK][tT]$")) {
					OpenRocketDocument doc = loadFromStream(in, container, motorFinder);
					in.close();
					return doc;
				}
			}
		}
		
		// Check for OpenRocket (individual .ork file)
		int match = 0;
		for (int i = 0; i < count; i++) {
			if (buffer[i] == OPENROCKET_SIGNATURE[match]) {
				match++;
				if (match == OPENROCKET_SIGNATURE.length) {
					return loadUsing(source, container, openRocketLoader, motorFinder);
				}
			} else {
				match = 0;
			}
		}
		
		// Check for RockSim file
		byte[] typeIdentifier = ArrayUtils.copyOf(buffer, ROCKSIM_SIGNATURE.length);
		if (Arrays.equals(ROCKSIM_SIGNATURE, typeIdentifier)) {
			return loadUsing(source, null, rocksimLoader, motorFinder);
		}
		throw new RocketLoadException("Unsupported or corrupt file.");
	}
	
	private OpenRocketDocument loadUsing(InputStream source, ZipFile container, RocketLoader loader, MotorFinder motorFinder)
			throws RocketLoadException {
		warnings.clear();
		OpenRocketDocument doc = loader.load(source, container, motorFinder);
		warnings.addAll(loader.getWarnings());
		return doc;
	}
}
