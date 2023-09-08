package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new ai.hellodoctor.DataBinderMapperImpl());
  }
}
