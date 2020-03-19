package com.jaakkomantyla.voicecode;

import com.jaakkomantyla.voicecode.VoiceParsingUtils.Utterance;

import org.junit.Test;


import static org.junit.Assert.assertEquals;


public class VoiceParsingTests {
    @Test
    public void parseUtterance() {
        // Context of the app under test.
        Utterance u = new Utterance(" keke  testailee ");
        Utterance u2 = new Utterance(" keke ei testaile");

        assertEquals(u.getWords().size(), 2);
        assertEquals(u2.getWords().size(), 3);
        assertEquals(u.getWords().get(0), "keke");
        assertEquals(u2.getWords().get(1), "ei");

    }
}
