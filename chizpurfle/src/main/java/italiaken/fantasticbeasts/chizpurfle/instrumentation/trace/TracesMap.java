package italiaken.fantasticbeasts.chizpurfle.instrumentation.trace;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.instrumentation.InstrumentationException;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.ITrace;

/**
 * Created by ken on 28/11/17 for fantastic_beasts
 */

public class TracesMap {

    private final JSONObject jsonString;
    private final Map<Class, Set<? extends ITrace>> map;

    public TracesMap(JSONObject deliveredMessage) throws InstrumentationException {

        this.jsonString = deliveredMessage;
        this. map = new HashMap<>();

        try {
            Iterator<String> iterator;

            JSONObject blocks = deliveredMessage.getJSONObject("blocks");
            Set<Block> blockSet = new HashSet<>();
            iterator = blocks.keys();
            while (iterator.hasNext()){
                String k = iterator.next();
                try {
                    blockSet.add(new Block(k, blocks.getLong(k)));
                } catch (NumberFormatException e) {
                    Log.w("problem parsing block "+k, e);
                }
            }
            map.put(Block.class, blockSet);

            JSONObject branches = deliveredMessage.getJSONObject("branches");
            Set<Branch> branchSet = new HashSet<>();
            iterator = branches.keys();
            while (iterator.hasNext()){
                String k = iterator.next();
                try {
                    branchSet.add(new Branch(k, branches.getLong(k)));
                } catch (NumberFormatException e) {
                    Log.w("problem parsing branch "+k, e);
                }
            }
            map.put(Branch.class, branchSet);

        } catch (JSONException e) {
            throw new InstrumentationException("can't to parse traces", e);
        }

    }

    public JSONObject getJsonString() {
        return jsonString;
    }

    public Map<Class, Set<? extends ITrace>> getMap() {
        return map;
    }

    public Set<Block> getBlocks() {
        return (Set<Block>) map.get(Block.class);
    }

    public Set<Branch> getBranches() {
        return (Set<Branch>) map.get(Branch.class);
    }
}
