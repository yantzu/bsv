package com.github.bsv;

import java.io.IOException;

public interface BsvDeserializer {
    Object next() throws IOException, BsvException;
}
