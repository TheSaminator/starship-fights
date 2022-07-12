# Star Cluster Programming Plan

## Data

* **StarCluster**
    * (unique) host -> Admiral
* **Admiral**
    * inCluster -> StarCluster?
    * invitedTo -> StarCluster[]
    * owner -> User
    * (unique) inCluster, owner
        * No User may have multiple Admiral records in a single StarCluster

## Actions

*The following code is written in somewhat-pretentious SQL-looking pseudocode*

* `Admiral.create(): StarCluster`
    * Prereq: `THIS.inCluster IS null`
    * Result: `CREATE StarCluster cluster(host: THIS); SET THIS.inCluster TO cluster`
* `Admiral.invite(other: Admiral)`
    * Prereq: `THIS.inCluster ISN'T NULL && THIS.inCluster.host IS THIS && other.inCluster IS NULL`
    * Result: `INTO other.invitedTo INSERT THIS.inCluster`
* `Admiral.acceptInvitation(cluster: StarCluster)`
    * Prereq: `THIS.inCluster IS NULL && THIS.invitedTo HAS cluster`
    * Result: `SET THIS.inCluster TO cluster && CLEAR THIS.invitedTo`
* `Admiral.rejectInvitation(cluster: StarCluster)`
    * Prereq: `THIS.invitedTo HAS cluster`
    * Result: `FROM THIS.invitedTo REMOVE cluster`
* `Admiral.kick(other: Admiral)`
    * Prereq: `THIS.inCluster ISN'T NULL && THIS.inCluster.host IS THIS && other ISN'T THIS && other.inCluster IS THIS.inCluster`
    * Result: `SET other.inCluster TO NULL`
* `Admiral.makeHost(other: Admiral)`
    * Prereq: `THIS.inCluster ISN'T NULL && THIS.inCluster.host IS THIS && other ISN'T THIS && other.inCluster IS THIS.inCluster`
    * Result: `SET THIS.inCluster.host TO other`
* `Admiral.quit()`
    * Prereq: `THIS.inCluster ISN'T NULL && THIS.inCluster.host ISN'T THIS`
    * Result: `SET THIS.inCluster TO NULL`
* `Admiral.deleteCluster(cluster: StarCluster)`
    * Prereq: `NO Admiral a EXISTS WHERE (a ISN'T THIS && a.inCluster IS cluster)`
    * Result: `OBLITERATE cluster`
