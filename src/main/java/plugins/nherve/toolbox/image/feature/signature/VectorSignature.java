package plugins.nherve.toolbox.image.feature.signature;

import plugins.nherve.toolbox.image.feature.Signature;

public interface VectorSignature extends Signature, Iterable<Integer> {
	void add(VectorSignature other) throws SignatureException;

	double get(int idx) throws SignatureException;

	int getNonZeroBins() throws SignatureException;

	int getSize();

	void normalizeL2(boolean force) throws SignatureException;
}
