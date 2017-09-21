package com.github.yantzu.bsv;

import java.io.IOException;

public interface BsvSerializer {
	void next(BsvObject obj) throws IOException, BsvException;

	void close() throws IOException, BsvException;
}
