package com.alipay.sofa.registry.jraft;

import com.alipay.sofa.registry.jraft.config.DefaultConfigs;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

/**
 * <h3>registry-parent</h3>
 * <p></p>
 *
 * @author : xingpeng
 * @date : 2021-06-21 21:27
 **/
@ActiveProfiles("test")
@SpringBootTest(classes = AbstractRaftTestBase.RaftTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AbstractRaftTestBase extends AbstractTest implements ApplicationContextAware{
    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

//    @After
//    public void deletRheaKVData(){
//        File databaseFile = new File(DefaultConfigs.DB_PATH);
//        File raftFile = new File(DefaultConfigs.RAFT_DATA_PATH);
//        if(databaseFile.exists()){
//            deleteFile(databaseFile);
//        }
//
//        if(raftFile.exists()){
//            deleteFile(raftFile);
//        }
//    }
//
//    private void deleteFile(File file){
//        File[] files=file.listFiles();/*获取该目录下得所有文件或者文件夹*/
//        if(files.length==0) {/*如果为空则直接退出*/
//            return;
//        }
//        System.out.println(file.getAbsolutePath());/*显示当前文件路劲*/
//        for(File f:files) {/*for 循环得一种便利方法*/
//            if(f.isFile()) {/*判断f是否是文件*/
//                f.delete();
//            }
//            else if(f.isDirectory()) {/*判断f是否是文件夹*/
//                deleteFile(f);
//            }
//            if(f.length()==0){
//                f.delete();
//            }
//        }
//        file.delete();
//    }

    @SpringBootApplication()
    public static class RaftTestConfig {}
}
