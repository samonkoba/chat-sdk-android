package co.chatsdk.core.dao;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.interfaces.CoreEntity;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.ConnectionType;
import timber.log.Timber;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS
// KEEP INCLUDES - put your token includes here

@Entity
public class User implements CoreEntity, UserListItem {

    @Id
    private Long id;
    private String entityID;
    private Integer authenticationType;
    private Date lastOnline;
    private Date lastUpdated;
    private String metaData;

    @ToMany(referencedJoinProperty = "userId")
    private List<LinkedAccount> linkedAccounts;

    @Transient
    private static final String TAG = User.class.getSimpleName();
    //@Transient
    //private static final boolean DEBUG = Debug.User;
    @Transient
    private static final String USER_PREFIX = "user";

    @Transient
    private Map<String, Object> metaMapCache = new HashMap();

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1507654846)
    private transient UserDao myDao;



    @Generated(hash = 130291976)
    public User(Long id, String entityID, Integer authenticationType, Date lastOnline, Date lastUpdated, String metaData) {
        this.id = id;
        this.entityID = entityID;
        this.authenticationType = authenticationType;
        this.lastOnline = lastOnline;
        this.lastUpdated = lastUpdated;
        this.metaData = metaData;
    }

    @Generated(hash = 586692638)
    public User() {
    }



    public List<User> getContacts() {
        return getContacts(ConnectionType.Contact);
    }

    public List<User> getContacts(ConnectionType type) {
        List<User> contactList = new ArrayList<>();

        // For some reason the default ContactLinks do not persist, have to find in DB
        List<ContactLink> contactLinks = DaoCore.fetchEntitiesWithProperty(ContactLink.class,
                ContactLinkDao.Properties.LinkOwnerUserDaoId, this.getId());

        for (ContactLink contactLink : contactLinks){
            if(contactLink.getConnectionType().equals(type)) {
                User user = contactLink.getUser();
                if(user != null && StringUtils.isNotEmpty(user.getName())) {
                    contactList.add(contactLink.getUser());
                }
            }
        }

        return contactList;
    }

    public void addContact(User user, ConnectionType type) {

        if (user.equals(this)) {
            return;
        }

        // Retrieve contacts
        List contacts = getContacts(type);

        // Check if user is already in contact list
        if (contacts.contains(user)) {
            return;
        }

        ContactLink contactLink = new ContactLink();
        contactLink.setConnectionType(type);
        // Set link owner
        contactLink.setLinkOwnerUser(this);
        contactLink.setLinkOwnerUserDaoId(this.getId());
        // Set contact
        contactLink.setUser(user);
        contactLink.setUserId(user.getId());
        // insert contact link entity into DB
        daoSession.insertOrReplace(contactLink);
        this.update();
    }

    public void deleteContact (User user) {
        deleteContact(user, ConnectionType.Contact);
    }

    public void deleteContact (User user, ConnectionType type) {
        // For some reason the default ContactLinks do not persist, have to find in DB
        Property [] properties = {
                ContactLinkDao.Properties.LinkOwnerUserDaoId,
                ContactLinkDao.Properties.Type
        };

        List<ContactLink> contactLinks = DaoCore.fetchEntitiesWithProperties(ContactLink.class,
                properties, this.getId(), type.ordinal());

        for(ContactLink link : contactLinks) {
            if(link.getUser().equals(user)) {
                DaoCore.deleteEntity(link);
            }
        }
        daoSession.update(this);
    }

    public void addContact(User user) {
        addContact(user, ConnectionType.Contact);
    }

    private FollowerLink fetchFollower(User follower, int type){
        return DaoCore.fetchEntityWithProperties(FollowerLink.class,
                new Property[]{FollowerLinkDao.Properties.UserId, FollowerLinkDao.Properties.LinkOwnerUserDaoId, FollowerLinkDao.Properties.Type},
                follower.getId(), getId(),  type);
    }

    public List<User> getFollowers() {
        List<User> users = new ArrayList<User>();

        List<FollowerLink> followers = DaoCore.fetchEntitiesWithProperties(FollowerLink.class,
                new Property[]{FollowerLinkDao.Properties.LinkOwnerUserDaoId, FollowerLinkDao.Properties.Type},
                getId(), FollowerLink.Type.FOLLOWER);

        for (FollowerLink f : followers)
        {
            if (f!=null)
                users.add(f.getUser());
        }

        return users;
    }

    public List<User> getFollows() {
        List<User> users = new ArrayList<User>();

        List<FollowerLink> followers = DaoCore.fetchEntitiesWithProperties(FollowerLink.class,
                new Property[]{FollowerLinkDao.Properties.LinkOwnerUserDaoId, FollowerLinkDao.Properties.Type},
                getId(), FollowerLink.Type.FOLLOWS);

        for (FollowerLink f : followers)
        {
            if (f!=null)
                users.add(f.getUser());
        }

        return users;
    }

    public FollowerLink fetchOrCreateFollower(User follower, int type) {

        FollowerLink follows = fetchFollower(follower, type);

        if (follows== null)
        {
            follows = new FollowerLink();

            follows.setLinkOwnerUser(this);
            follows.setUser(follower);
            follows.setType(type);

            follows = DaoCore.createEntity(follows);
        }

        return follows;
    }

    public boolean isFollowing(User user){
        return fetchFollower(user, FollowerLink.Type.FOLLOWER) != null;
    }

    public boolean follows(User user){
        return fetchFollower(user, FollowerLink.Type.FOLLOWS) != null;
    }

    public void setAvatarURL(String imageUrl, String hash) {
        setAvatarURL(imageUrl);
        setAvatarHash(hash);
    }

    public void setAvatarURL(String imageUrl) {
        setMetaString(Keys.AvatarURL, imageUrl);
    }

    public void setPresenceSubscription (String presence) {
        setMetaString(Keys.PresenceSubscription, presence);
    }

    public String getPresenceSubscription () {
        return metaStringForKey(Keys.PresenceSubscription);
    }

    public String getAvatarURL() {
        return metaStringForKey(Keys.AvatarURL);
    }

    public void setAvatarHash(String hash) {
        setMetaString(Keys.AvatarHash, hash);
    }

    public String getAvatarHash() {
        return metaStringForKey(Keys.AvatarHash);
    }

    // Use getAvatarURL
    @Deprecated
    public String getThumbnailURL() {
        return getAvatarURL();
    }

    // Use getAvatarURL
    @Deprecated
    public void setThumbnailURL(String thumbnailUrl) {
        setMetaString(Keys.AvatarThumbnailURL, thumbnailUrl);
    }

    public void setName(String name) {
        setMetaString(Keys.Name, name);
    }

    public String getName() {
        return metaStringForKey(Keys.Name);
    }

    public void setEmail(String email) {
        setMetaString(Keys.Email, email);
    }

    public String getEmail() {
        return metaStringForKey(Keys.Email);
    }

    public String getCountryCode () {
        return metaStringForKey(Keys.CountryCode);
    }

    public void setCountryCode (String code) {
        setMetaString(Keys.CountryCode, code);
    }

    public void setDateOfBirth (String date) {
        setMetaString(Keys.DateOfBirth, date);
    }

    public void setStatus (String status) {
        setMetaString(Keys.Status, status);
    }

    public String getStatus () {
        return metaStringForKey(Keys.Status);
    }

    public String getDateOfBirth () {
        return metaStringForKey(Keys.DateOfBirth);
    }

    public String getPhoneNumber () {
        return metaStringForKey(Keys.Phone);
    }

    public void setPhoneNumber (String phoneNumber) {
        setMetaString(Keys.Phone, phoneNumber);
    }

    public void setAvailability (String availability) {
        setMetaString(Keys.Availability, availability);
    }

    public String getState () {
        return metaStringForKey(Keys.State);
    }

    public void setState (String state) {
        setMetaString(Keys.State, state);
    }

    public String getAvailability () {
        return metaStringForKey(Keys.Availability);
    }

    public String getLocation () {
        return metaStringForKey(Keys.Location);
    }

    public void setLocation (String location) {
        setMetaString(Keys.Location, location);
    }

    public String metaStringForKey(String key) {
        return (String) metaMap().get(key);
    }

    public void setMetaString(String key, String value){
        Map<String, Object> map = metaMap();
        map.put(key, value);
        
        setMetaMap(map);
        DaoCore.updateEntity(this);
    }

    /**
     * Setting the metaData, The Map will be converted to a Json String.
     **/
    public void setMetaMap(Map<String, Object> metadata){
        metadata = updateMetaDataFormat(metadata);
        setMetaData(new JSONObject(metadata).toString());
    }


    @Deprecated
    /**
     * This is for maintaining compatibility with older chat versions, It will be removed in a few versions.
     **/
    private Map<String, Object> updateMetaDataFormat(Map<String, Object> map){
        
        Map<String, Object> newData = new HashMap<>();

        String newKey, value;
        for (String key : map.keySet())
        {
            if (map.get(key) instanceof Map)
            {
                value = (String) ((Map) map.get(key)).get(Keys.Value);
                newKey = (String) ((Map) map.get(key)).get(Keys.Key);
                newData.put(newKey, value);
                
                //if (DEBUG) Timber.i("convertedData, Key: %s, Value: %s", newKey, value);
            }
            else 
                newData.put(key, map.get(key));
        }
        
        return newData;
    }

    /**
     * Converting the metaData json to a map object
     **/
    public Map<String, Object> metaMap() {
        if(metaMapCache == null || metaMapCache.keySet().size() == 0) {
            if(metaData != null) {
                try {
                    metaMapCache = JsonHelper.toMap(new JSONObject(metaData));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Timber.e(e.getCause(), "Cant parse metaData json to map. Meta: %s", metaData);
                }
            }
            if(metaMapCache == null) {
                metaMapCache = new HashMap<>();
            }
        }
        return metaMapCache;
    }

    public boolean hasThread(Thread thread){
        UserThreadLink data =
                DaoCore.fetchEntityWithProperties(UserThreadLink.class,
                        new Property[]{UserThreadLinkDao.Properties.ThreadId, UserThreadLinkDao.Properties.UserId}, thread.getId(), getId());

        return data != null;
    }

    public String getPushChannel(){
        if (entityID == null)
            return "";

        return USER_PREFIX + (entityID.replace(":","_"));
    }

    public boolean isMe(){
        return getId().longValue() == NM.currentUser().getId().longValue();
    }

    public String toString() {
        return String.format("User, id: %s meta: %s", id, getMetaData());
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getEntityID() {
        return this.entityID;
    }


    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }


    public Integer getAuthenticationType() {
        return this.authenticationType;
    }


    public void setAuthenticationType(Integer AuthenticationType) {
        this.authenticationType = AuthenticationType;
    }

    public java.util.Date getLastOnline() {
        return this.lastOnline;
    }


    public void setLastOnline(java.util.Date lastOnline) {
        this.lastOnline = lastOnline;
    }


    public java.util.Date getLastUpdated() {
        return this.lastUpdated;
    }


    public void setLastUpdated(java.util.Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setMetaData(String Metadata) {
        this.metaData = Metadata;
        clearMetaMapCache();
    }

    public void clearMetaMapCache () {
        metaMapCache = null;
    }

    public String getMetaData() {
        return this.metaData;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 2037294643)
    public List<LinkedAccount> getLinkedAccounts() {
        if (linkedAccounts == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LinkedAccountDao targetDao = daoSession.getLinkedAccountDao();
            List<LinkedAccount> linkedAccountsNew = targetDao._queryUser_LinkedAccounts(id);
            synchronized (this) {
                if (linkedAccounts == null) {
                    linkedAccounts = linkedAccountsNew;
                }
            }
        }
        return linkedAccounts;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1903600659)
    public synchronized void resetLinkedAccounts() {
        linkedAccounts = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2059241980)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserDao() : null;
    }


}