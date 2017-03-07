package com.github.bsv;

import java.io.IOException;
import java.io.InputStream;

public interface BsvSchemaParser {
    BsvSchema parse(InputStream inputStream) throws IOException;
}
