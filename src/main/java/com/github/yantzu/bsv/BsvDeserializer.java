package com.github.yantzu.bsv;

import java.io.IOException;

public interface BsvDeserializer {

	BsvObject next() throws IOException, BsvException;

	void close() throws IOException, BsvException;
}
