package org.sidoh.io;

import java.io.File;

public class IOHelper {
	/**
	 * Recursively remove directory
	 * 
	 * Taken from: http://www.exampledepot.com/egs/java.io/DeleteDir.html
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}
	
	/**
	 * Get relative path from two absolute paths.
	 * 
	 * @return
	 */
	public static String getRelativePath(File dir, File child) {
		return dir.getAbsoluteFile().toURI().relativize(child.getAbsoluteFile().toURI()).getPath();
	}
	
	public static File toSequenceFile(File file) {
		return toSequenceFile(file.getAbsolutePath());
	}
	
	public static File toSequenceFile(String file) {
		File b = new File(file);
		File r = new File(file);
		String extension = b.getName().substring(b.getName().lastIndexOf('.')+1);
		String base = b.getName().substring(0, b.getName().lastIndexOf('.'));
		int i = 2;
		
		while (r.exists()) {
			r = new File(b.getParentFile(), String.format("%s-%d.%s", base, i++, extension));
		}
		
		return r;
	}
}
