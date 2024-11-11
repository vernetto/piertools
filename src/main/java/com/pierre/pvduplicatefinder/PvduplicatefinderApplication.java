package com.pierre.pvduplicatefinder;

import java.io.IOException;
import java.util.List;

public class PvduplicatefinderApplication {

    FinderService finderService = new FinderService();

    public static void main(String[] args) throws IOException {
        PvduplicatefinderApplication pvduplicatefinderApplication  = new PvduplicatefinderApplication();
        pvduplicatefinderApplication.find();
    }

    public void find() throws IOException {
        List<DuplicateCollection> result = finderService.findDuplicates("D:\\", "S:\\");
        System.out.println(result);
    }


}
