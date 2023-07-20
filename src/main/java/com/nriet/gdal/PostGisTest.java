package com.nriet.gdal;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

public class PostGisTest {

//	@Test
	public void pgGeom() {
		ogr.RegisterAll();
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
		gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
		// 获取pg驱动
		String pgDriverName = "PostgreSQL";
		org.gdal.ogr.Driver pgDriver = ogr.GetDriverByName(pgDriverName);
		if (pgDriver == null) {
			System.out.println("不支持" + pgDriverName + "驱动");
			return;
		}

		// GDAL连接PostGIS
		String path = "PG:dbname=postgres host=124.221.146.213 port=15432 user=postgres password=nriet123";

		Dataset ds = gdal.Open(path);
		DataSource pgDataSource = pgDriver.Open(path, 0);
		if (pgDataSource == null) {
			System.out.println("GDAL连接PostGIS数据库失败!");
			return;
		}
		String strSQL = "SELECT * from nj_bu where gid=1";
		// 获取图层
		Layer pgLayer = pgDataSource.ExecuteSQL(strSQL);
		// Layer pgLayer = pgDataSource.GetLayerByName("123");
		if (pgLayer == null) {
			System.out.println("获取【" + "province" + "】图层失败！");
			return;
		}
		System.out.println(pgLayer.GetFIDColumn());

		// 创建矢量文件
//		String strVectorFile = "C:\\data\\pg2shp.shp";
		// 驱动
//		String shpDriverName = "ESRI Shapefile";
//		org.gdal.ogr.Driver shpDriver = ogr.GetDriverByName(shpDriverName);
//		if (shpDriver == null) {
//			System.out.println(shpDriverName + " 驱动不可用！\n");
//			return;
//		}
		// 数据源
//		DataSource shpDataSource = shpDriver.CreateDataSource(strVectorFile, null);
//		if (shpDataSource == null) {
//			System.out.println("创建矢量文件【" + strVectorFile + "】失败！\n");
//			return;
//		}
		// 图层
//		Layer shpLayer = shpDataSource.CreateLayer("", pgLayer.GetSpatialRef(), pgLayer.GetGeomType());
//		if (shpLayer == null) {
//			System.out.println("图层创建失败！\n");
//			return;
//		}
		// 字段
		FeatureDefn pgDefn = pgLayer.GetLayerDefn();
		int iFieldCount = pgDefn.GetFieldCount();
		for (int i = 0; i < iFieldCount; i++) {
			FieldDefn oField = pgDefn.GetFieldDefn(i);
//			shpLayer.CreateField(oField, 1);
			System.out.println(oField.GetName());
		}
		// 数据记录
		Feature oFeature = null;
		while ((oFeature = pgLayer.GetNextFeature()) != null) {
			System.out.println(oFeature.GetGeomFieldRef("geom").GetGeometryCount());

//			shpLayer.CreateFeature(oFeature);
		}
		// 写入文件
//		shpLayer.SyncToDisk();
//		shpDataSource.SyncToDisk();
		// 删除数据源
		pgDataSource.delete();
//		shpDataSource.delete();

		gdal.GDALDestroyDriverManager();
		System.out.println("shp文件创建成功！");
	}

	public void pgRaster() {
		long t1=System.currentTimeMillis();
		ogr.RegisterAll();
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
		gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
		// 获取pg驱动
		String pgDriverName = "PostgreSQL";
		org.gdal.ogr.Driver pgDriver = ogr.GetDriverByName(pgDriverName);
		if (pgDriver == null) {
			System.out.println("不支持" + pgDriverName + "驱动");
			return;
		}
		// GDAL连接PostGIS
		String path = "PG:dbname=postgres schema=data host=124.221.146.213 port=15432 user=postgres password=nriet123 table='ecmwf_tmp' mode=2";
		Dataset dataset = gdal.Open(path);
//		dataset.ExecuteSQL("");
		if (dataset != null) {
			// 获取栅格数据集的宽度和高度
			int width = dataset.GetRasterXSize();
			int height = dataset.GetRasterYSize();

			// 获取栅格数据集的波段数
			int bandCount = dataset.getRasterCount();

			// 读取栅格数据
			for (int bandIndex = 1; bandIndex <= bandCount; bandIndex++) {
				// 获取波段对象
				org.gdal.gdal.Band band = dataset.GetRasterBand(bandIndex);

				// 创建存储栅格数据的数组
				float[] data = new float[width * height];

				// 读取栅格数据到数组
				band.ReadRaster(0, 0, width, height, data);
//				for (int i = 0; i < data.length; i++) {
//					data[i]=data[i] + 258.1118936108376f - 273.15f;
//				}
				
				
				// 在此处处理栅格数据，例如进行数据分析或显示等操作
				// ...

				// 释放波段对象内存
				band.delete();
			}
		}

		// 释放资源
		dataset.delete();
		gdal.GDALDestroyDriverManager();

		long t2=System.currentTimeMillis();
		
		System.out.println(t2-t1);
	}
}
