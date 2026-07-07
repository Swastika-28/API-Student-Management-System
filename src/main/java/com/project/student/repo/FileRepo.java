package com.project.student.repo;

import org.springframework.stereotype.Repository;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Repository
public class FileRepo {
    public File getFile(String uploadDir,String fileName){
        Path filePath= Paths.get(uploadDir).resolve(fileName).normalize();
        return filePath.toFile();
    }
    public boolean fileExists(File file){
        return file.exists() && file.isFile();
    }


}
