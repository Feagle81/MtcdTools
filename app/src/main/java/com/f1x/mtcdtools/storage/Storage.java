package com.f1x.mtcdtools.storage;

import com.f1x.mtcdtools.storage.exceptions.DuplicatedEntryException;
import com.f1x.mtcdtools.storage.exceptions.EntryCreationFailed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Created by f1x on 2017-01-29.
 */

public abstract class Storage<Key, Value> {
    public Storage(FileReader reader, FileWriter writer) {
        mReader = reader;
        mWriter = writer;
        mItems = createContainer();
    }

    public void insert(Key key, Value item) throws JSONException, IOException, DuplicatedEntryException {
        put(key, item);
        write();
    }

    public void remove(Key key) throws IOException, JSONException {
        if(mItems.containsKey(key)) {
            mItems.remove(key);
            write();
        }
    }

    public void replace(Key oldKey, Key newKey, Value newItem) throws JSONException, IOException, DuplicatedEntryException {
        if(mItems.containsKey(oldKey)) {
            if (!oldKey.equals(newKey) && mItems.containsKey(newKey)) {
                throw new DuplicatedEntryException(newKey.toString());
            }

            mItems.remove(oldKey);
            mItems.put(newKey, newItem);
            write();
        }
    }

    public Value getItem(Key key) {
        return mItems.containsKey(key) ? mItems.get(key) : null;
    }

    public Map<Key, Value> getItems() {
        Map<Key, Value> copy = createContainer();
        copy.putAll(mItems);
        return copy;
    }

    public abstract void read() throws JSONException, IOException, DuplicatedEntryException, EntryCreationFailed;
    public abstract void write() throws JSONException, IOException;

    final JSONArray read(String fileName, String arrayName) throws IOException, JSONException {
        String inputString = mReader.read(fileName, CHARSET);
        if(!inputString.isEmpty()) {
            JSONObject inputsJson = new JSONObject(inputString);
            return inputsJson.getJSONArray(arrayName);
        }

        return new JSONArray();
    }

    final void write(String fileName, String arrayName, JSONArray array) throws IOException, JSONException {
        JSONObject inputsJson = new JSONObject();
        inputsJson.put(arrayName, array);
        mWriter.write(inputsJson.toString(), fileName, CHARSET);
    }

    void put(Key key, Value value) throws DuplicatedEntryException {
        if(mItems.containsKey(key)) {
            throw new DuplicatedEntryException(key.toString());
        } else {
            mItems.put(key, value);
        }
    }

    protected abstract Map<Key, Value> createContainer();

    private final FileReader mReader;
    private final FileWriter mWriter;
    protected final Map<Key, Value> mItems;

    private static final String CHARSET = "UTF-8";
}
