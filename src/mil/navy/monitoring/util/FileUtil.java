package mil.navy.monitoring.util;

import java.io.File;

public class FileUtil {

	public static void deleteAllFiles(String path){
		
		File file = new File(path);
		//폴더내 파일을 배열로 가져온다.
		File[] tempFile = file.listFiles();

		if(tempFile.length >0){
			
			try
			{
				for (int i = 0; i < tempFile.length; i++) {
					
					if(tempFile[i].isFile()){
						System.out.println(tempFile[i].getPath() + " was Deleted");
						tempFile[i].delete();
					}else{
						//재귀함수
						deleteAllFiles(tempFile[i].getPath());
					}
					tempFile[i].delete();
					System.out.println(tempFile[i].getPath() + " was Deleted");
				}
				file.delete();
				System.out.println(file.getPath() + " was Deleted");
			}catch (NullPointerException e)
			{
				
			}
			
			
		}
		
	}
	
}
