package com.dbscanapp.TextFileHelper;

import java.util.List;

/**FileHelper sınıfı daha önceden yazmış olduğum bir sınıf.
 * Başka yerlerde kullanabilmem için genel bir interface yazmıştım.*/
public interface ITextDataParser<T> {

    List<T> parseFromFile(List<String> lines);
    List<String> parseFromObjects(List<T> objects);

}
