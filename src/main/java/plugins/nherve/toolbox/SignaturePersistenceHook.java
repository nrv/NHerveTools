package plugins.nherve.toolbox;

import java.io.IOException;
import java.nio.channels.FileChannel;

import plugins.nherve.toolbox.image.feature.signature.VectorSignature;

public interface SignaturePersistenceHook<T extends VectorSignature> {
	int getTypeCode();
	Class<T> getSignatureClass();
	void dumpSignature(FileChannel fc, VectorSignature s) throws IOException;
	VectorSignature loadSignature(FileChannel fc) throws IOException;
}
