package plugins.nherve.toolbox;

import java.io.IOException;
import java.nio.channels.FileChannel;

import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;

public interface SignaturePersistenceHook<T extends DefaultVectorSignature> {
	int getTypeCode();
	Class<T> getSignatureClass();
	void dumpSignature(FileChannel fc, DefaultVectorSignature s) throws IOException;
	DefaultVectorSignature loadSignature(FileChannel fc) throws IOException;
}
