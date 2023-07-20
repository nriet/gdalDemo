package com.nriet.gdal;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.osr;

public class ShpTest {
    static {
        gdal.AllRegister();
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","YES");
        gdal.SetConfigOption("SHAPE_ENCODING","CP936");
        gdal.SetConfigOption("FILEGDB_ENCODING","UTF-8");
        osr.SetPROJSearchPath(System.getenv("JAVA_HOME") + "\\bin");
    }

    public static void main(String[] args) {
        resolutionSHPFile("C:\\Users\\22583\\git\\nriet-spring-cloud\\nriet-datacenter\\src\\main\\resources\\shp\\county.shp");
    	System.out.println("测试");
    }
    /**
     * 解析shp文件信息
     * @param srcFile
     */
    public static void resolutionSHPFile(String srcFile) {
        // 注册所有的驱动
        ogr.RegisterAll();
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","YES");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING","CP936");
        //读取数据，这里以ESRI的shp文件为例
        String strDriverName = "ESRI Shapefile";
        //创建一个文件，根据strDriverName扩展名自动判断驱动类型
        Driver oDriver =ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strDriverName+ "驱动没有初始化");
            return;
        }
        // 读取shp文件
        DataSource srcDataSource = ogr.Open(srcFile, 0);
        if (srcDataSource == null) {
            System.err.println("文件读取错误" + gdal.GetLastErrorNo());
            System.err.println(gdal.GetLastErrorMsg());
            return;
        }
        // 获取该数据源中的图层个数，如果小于1返回错误，shp只有一个图层，mdb会有多个
        int layerCount = srcDataSource.GetLayerCount();
        System.out.println("图层数 = " + layerCount);
        if (layerCount < 1) {
            System.err.println("异常：没有图层。");
            return;
        }
        Layer layer = srcDataSource.GetLayer(0);

        // 获取shp文件四角坐标范围
        double[] extent = layer.GetExtent(true);
        System.out.println("extent is " + extent[0] + ", " + extent[1] + ", " + extent[2] + ", " + extent[3]);

        // 获取图层中的属性表表头并输出
        System.out.println("属性表结构信息：");
        FeatureDefn featureDefn = layer.GetLayerDefn();
        int fieldCount = featureDefn.GetFieldCount();
        for (int index = 0; index < fieldCount; index++) {
            FieldDefn fieldDefn = featureDefn.GetFieldDefn(index);
            System.out.println(fieldDefn.GetNameRef() + ":" +
                    fieldDefn.GetFieldTypeName(fieldDefn.GetFieldType()) + "(" +
                    fieldDefn.GetWidth() + "." +
                    fieldDefn.GetPrecision() + ")");
        }
        // 输出图层中的要素个数
        System.out.println("要素个数 = " + layer.GetFeatureCount(0));

        Feature feature = layer.GetNextFeature();
        while (feature != null) {
            StringBuffer sb = new StringBuffer();
            System.out.println("当前处理第：" + feature.GetFID() + "条");
            // 获取要素中的属性表内容
            for (int index = 0; index < fieldCount; index++) {
                FieldDefn fieldDefn = featureDefn.GetFieldDefn(index);
                sb.append(fieldDefn.GetNameRef());
                sb.append("(");
                sb.append(ogr.GetFieldTypeName(fieldDefn.GetFieldType()));
                sb.append(") = ");
                if (feature.IsFieldSet(index)) {
                    sb.append(feature.GetFieldAsString(index)).append("  ");
                } else {
                    sb.append("(null)");
                }
            }
            System.out.println(sb.toString());
            // 获取要素中的几何体
            Geometry geometry = feature.GetGeometryRef();
            System.out.println("空间属性JSON:"+ geometry.ExportToJson());
            feature = layer.GetNextFeature();
        }
        srcDataSource.delete();
    }
}