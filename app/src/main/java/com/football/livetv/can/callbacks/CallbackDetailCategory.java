package com.football.livetv.can.callbacks;

import com.football.livetv.can.models.Category;
import com.football.livetv.can.models.Channel;

import java.util.ArrayList;
import java.util.List;

public class CallbackDetailCategory {

    public String status = "";
    public int count_total = -1;

    public Category category = null;
    public List<Channel> posts = new ArrayList<>();

}
