package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author 丰源
 * @date 创建时间：2017年4月19日 上午9:43:21
 * JDBC层：就是对数据库进行增删改查
 * Util：工具
 * DBUtil:JDBC工具类:
 * 1.数据库连接
 * 2.关闭各种资源
 * 3.通用的增删改
 * 4.通用查询
 */
public class DBUtil2 {
	private static String url;
	private static String user;
	private static String password;
	private static String driver;
	
	static{
		System.out.println("Hello");
		init();
	}
	
	public static void init(){
		//Properties加载properties配置文件的工具类
		Properties prop = new Properties();
		try {
			//DBUtil.class.getClassLoader().getResourceAsStream():加载src根目录下的文件
			//DBUtil.class.getResourceAsStream():加载DBUtil所在包下的文件
			InputStream in = DBUtil2.class.getClassLoader().
					getResourceAsStream("db.properties");
			//new FileInputStream("文件名")：加载项目根目录下的文件
//			InputStream in = new FileInputStream("db.properties");
			//把Properties对象和字节输入流关联
			prop.load(in);
			//Properties.getProperty(key):value
			url = prop.getProperty("url");
			user = prop.getProperty("user");
			password = prop.getProperty("password");
			driver = prop.getProperty("driver");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//获取连接:该方法中不要关闭连接，在业务代码/增删改操作中关闭连接
	public static Connection getConnection(){
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	//关闭各种资源
	public static void closeAll(Connection conn,PreparedStatement pstmt,ResultSet rs){
		try {
			if(rs!=null){
				rs.close();
			}
			if(pstmt!=null){
				pstmt.close();
			}
			if(conn!=null){
				conn.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//通用的增删改
	//params:占位符的值数组
	public static int generalUpdate(String sql,Object... params){
		int result = -1;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			//1.获取数据库连接
			conn = getConnection();
			//2.获取PreparedStatement
			pstmt = conn.prepareStatement(sql);
			//3.给占位符赋值
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i+1, params[i]);
			}
			//4.executeUpdate:执行sql
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally{
			closeAll(conn, pstmt, null);
		}
		return result;
	}
	
	//通用增删改
	//X:就是个占位符，相当于?，后期调用时传的是什么类型就是什么类型
	public static <X> List<X> selectX(String sql,Object[] params,Class<X> cls){  
		List<X> list = new ArrayList<X>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		//ResultSet:结果集
		ResultSet rs = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			if(params!=null){
				for (int i = 0; i < params.length; i++) {
					pstmt.setObject(i+1, params[i]);
				}
			}
			rs = pstmt.executeQuery();
			//ResultSetMetaData：该对象封装了列的个数和列名
			ResultSetMetaData md = rs.getMetaData();
			//column:列
			//row:行
			//获取列数:md.getColumnCount()
			int lieShu = md.getColumnCount();
			while(rs.next()){
				//new对象
				X x = cls.newInstance();
				//rs.next():取一行
				//每行都有这些列
				for(int i=1;i<=lieShu;i++){
					//通过列号获取列名
					String lieMing = md.getColumnName(i);
					//通过列名获取类的属性
					//Field:属性
					Field field = cls.getDeclaredField(lieMing);
					//属性变为可见
					field.setAccessible(true);
					//field.set(对象名,该属性值);
					field.set(x, rs.getObject(lieMing));
				}
				list.add(x);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally{
			closeAll(conn, pstmt, rs);
		}
		return list;
	}
	
	public static void main(String[] args) {
		System.out.println(getConnection());
	}
}
