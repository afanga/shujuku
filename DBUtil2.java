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
 * @author ��Դ
 * @date ����ʱ�䣺2017��4��19�� ����9:43:21
 * JDBC�㣺���Ƕ����ݿ������ɾ�Ĳ�
 * Util������
 * DBUtil:JDBC������:
 * 1.���ݿ�����
 * 2.�رո�����Դ
 * 3.ͨ�õ���ɾ��
 * 4.ͨ�ò�ѯ
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
		//Properties����properties�����ļ��Ĺ�����
		Properties prop = new Properties();
		try {
			//DBUtil.class.getClassLoader().getResourceAsStream():����src��Ŀ¼�µ��ļ�
			//DBUtil.class.getResourceAsStream():����DBUtil���ڰ��µ��ļ�
			InputStream in = DBUtil2.class.getClassLoader().
					getResourceAsStream("db.properties");
			//new FileInputStream("�ļ���")��������Ŀ��Ŀ¼�µ��ļ�
//			InputStream in = new FileInputStream("db.properties");
			//��Properties������ֽ�����������
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
	
	//��ȡ����:�÷����в�Ҫ�ر����ӣ���ҵ�����/��ɾ�Ĳ����йر�����
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
	
	//�رո�����Դ
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
	
	//ͨ�õ���ɾ��
	//params:ռλ����ֵ����
	public static int generalUpdate(String sql,Object... params){
		int result = -1;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			//1.��ȡ���ݿ�����
			conn = getConnection();
			//2.��ȡPreparedStatement
			pstmt = conn.prepareStatement(sql);
			//3.��ռλ����ֵ
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i+1, params[i]);
			}
			//4.executeUpdate:ִ��sql
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally{
			closeAll(conn, pstmt, null);
		}
		return result;
	}
	
	//ͨ����ɾ��
	//X:���Ǹ�ռλ�����൱��?�����ڵ���ʱ������ʲô���;���ʲô����
	public static <X> List<X> selectX(String sql,Object[] params,Class<X> cls){  
		List<X> list = new ArrayList<X>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		//ResultSet:�����
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
			//ResultSetMetaData���ö����װ���еĸ���������
			ResultSetMetaData md = rs.getMetaData();
			//column:��
			//row:��
			//��ȡ����:md.getColumnCount()
			int lieShu = md.getColumnCount();
			while(rs.next()){
				//new����
				X x = cls.newInstance();
				//rs.next():ȡһ��
				//ÿ�ж�����Щ��
				for(int i=1;i<=lieShu;i++){
					//ͨ���кŻ�ȡ����
					String lieMing = md.getColumnName(i);
					//ͨ��������ȡ�������
					//Field:����
					Field field = cls.getDeclaredField(lieMing);
					//���Ա�Ϊ�ɼ�
					field.setAccessible(true);
					//field.set(������,������ֵ);
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
