package com.latino.krakolo.ilakra.futbol.mystreamdht.callbacks;


import com.latino.krakolo.ilakra.futbol.mystreamdht.models.Category;
import com.latino.krakolo.ilakra.futbol.mystreamdht.models.Channel;

import java.util.ArrayList;
import java.util.List;

public class CallbackDetailCategory {

    public String status = "";
    public int count_total = -1;

    public Category category = null;
    public List<Channel> posts = new ArrayList<>();

}
