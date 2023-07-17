package com.loafer.core.convert;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class UserInfoConvert implements Converter<String, String> {

    @Override
    public String convert(MappingContext<String, String> mappingContext) {
        /*String source = mappingContext.getSource();
        int length = source.length();
        String prefix = source.substring(0, 1);
        String suffix = source.substring(length - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - 2; i++) {
            sb.append("*");
        }
        return length > 2 ? prefix + sb + suffix
                : length > 1 ? "*" + suffix : prefix;*/
        return mappingContext.getSource();
    }

}
