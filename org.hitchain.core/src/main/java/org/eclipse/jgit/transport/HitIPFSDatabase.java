/*******************************************************************************
 * Copyright (c) 2019-03-04 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.eclipse.jgit.transport;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.*;
import org.hitchain.core.HitIPFSStorage;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * HitIPFSDatabase
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-04
 * auto generate by qdp.
 */
public class HitIPFSDatabase extends WalkRemoteObjectDatabase {
    private HitIPFSStorage hit;

    public HitIPFSDatabase(HitIPFSStorage hit) {
        this.hit = hit;
    }

    /**
     * default path is point to "objects/".
     *
     * @param path
     * @return
     */
    protected String resolvePath(String path) {
        boolean toRoot = StringUtils.startsWith(path, ROOT_DIR);
        if (toRoot) {
            path = path.substring(ROOT_DIR.length());
        }
        path = StringUtils.removeStart(path, "/");
        path = StringUtils.removeEnd(path, "/");
        return toRoot ? path : ("objects/" + path);
    }

    public URIish getURI() {
        URIish u = new URIish();
        u = u.setScheme(TransportHit.HIT_SCHEME);
        u = u.setHost("www.hitchain.org");
        u = u.setPath("/");
        return u;
    }

    public Collection<WalkRemoteObjectDatabase> getAlternates() throws IOException {
        try {
            return readAlternates(INFO_ALTERNATES);
        } catch (FileNotFoundException err) {
            // Fall through.
        }
        return null;
    }

    public WalkRemoteObjectDatabase openAlternate(String location) throws IOException {
        return new HitIPFSDatabase(hit);
    }

    public Collection<String> getPackNames() throws IOException {
        HashSet<String> have = new HashSet<>();
        have.addAll(hit.list("objects/pack"));

        Collection<String> packs = new ArrayList<>();
        for (String n : have) {
            if (!n.startsWith("pack-") || !n.endsWith(".pack")) {
                continue;
            }
            String in = n.substring(0, n.length() - 5) + ".idx";
            if (have.contains(in)) {
                packs.add(n);
            }
        }
        return packs;
    }

    public FileStream open(String path) throws IOException {
        byte[] bs = hit.get(resolvePath(path));
        bs = bs == null ? new byte[0] : bs;
        int len = bs.length;
        return new FileStream(new ByteArrayInputStream(bs), len);
    }


    public void deleteFile(String path) throws IOException {
        hit.delete(resolvePath(path));
    }

    public OutputStream writeFile(String path, ProgressMonitor monitor, String monitorTask) throws IOException {
        return hit.beginPut(resolvePath(path), monitor, monitorTask);
    }

    public void writeFile(String path, byte[] data) throws IOException {
        hit.put(resolvePath(path), data);
    }

    protected Map<String, Ref> readAdvertisedRefs() throws TransportException {
        TreeMap<String, Ref> avail = new TreeMap<>();
        readPackedRefs(avail);
        readLooseRefs(avail);
        readRef(avail, Constants.HEAD);
        return avail;
    }

    protected void readLooseRefs(TreeMap<String, Ref> avail) throws TransportException {
        try {
            for (String n : hit.list("refs")) {
                readRef(avail, "refs/" + n);
            }
        } catch (IOException e) {
            throw new TransportException(getURI(), JGitText.get().cannotListRefs, e);
        }
    }

    protected Ref readRef(TreeMap<String, Ref> avail, String rn) throws TransportException {
        String s;
        String ref = ROOT_DIR + rn;// to root dir.
        try {
            try (BufferedReader br = openReader(ref)) {
                s = br.readLine();
            }
        } catch (FileNotFoundException noRef) {
            return null;
        } catch (IOException err) {
            throw new TransportException(getURI(), MessageFormat.format(JGitText.get().transportExceptionReadRef, ref), err);
        }

        if (s == null) {
            throw new TransportException(getURI(), MessageFormat.format(JGitText.get().transportExceptionEmptyRef, rn));
        }

        if (s.startsWith("ref: ")) {
            String target = s.substring("ref: ".length());
            Ref r = avail.get(target);
            if (r == null) {
                r = readRef(avail, target);
            }
            if (r == null) {
                r = new ObjectIdRef.Unpeeled(Ref.Storage.NEW, target, null);
            }
            r = new SymbolicRef(rn, r);
            avail.put(r.getName(), r);
            return r;
        }

        if (ObjectId.isId(s)) {
            Ref r = new ObjectIdRef.Unpeeled(loose(avail.get(rn)), rn, ObjectId.fromString(s));
            avail.put(r.getName(), r);
            return r;
        }

        throw new TransportException(getURI(), MessageFormat.format(JGitText.get().transportExceptionBadRef, rn, s));
    }

    protected Ref.Storage loose(Ref r) {
        if (r != null && r.getStorage() == Ref.Storage.PACKED) {
            return Ref.Storage.LOOSE_PACKED;
        }
        return Ref.Storage.LOOSE;
    }


    public void close() {
        // We do not maintain persistent connections.
    }
}
