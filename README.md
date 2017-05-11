# AndroidUtil

主要用于收集工作中使用到的工具。


## AndroidBmpUtil

* 将PNG图片转为单色Window Bitmap


```java
    byte[] bytes = AndroidBmpUtil.convert(bitmap);
```


## AndroidPbmUtil

* 将PNG图片转为PBM p4格式图片

```
    boolean isSuccess = pbmUtil.convertP4File(pbm4FilePath, cacheBitmap);
```

> [JbigKit](https://www.cl.cam.ac.uk/~mgk25/git/jbigkit) 一个可以将pbm格式图片转为jbg格式的C库