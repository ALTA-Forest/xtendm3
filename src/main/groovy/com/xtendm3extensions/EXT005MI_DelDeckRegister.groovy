// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete deck register from EXTDPR
// Transaction DelDeckRegister
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: TRNO - Transaction Number
 * 
*/


 public class DelDeckRegister extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
    double inLOAD
    double inTLOG
    double inTGBF 
    double inTNBF 
    double detailLOAD
    double detailDLOG
    double detailGVBF
    double detailNVBF

  
  // Constructor 
  public DelDeckRegister(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
     this.miCaller = miCaller
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Deck ID
     int inDPID     
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0    
     }

     // Transaction Number
     int inTRNO    
     if (mi.in.get("TRNO") != null) {
        inTRNO = mi.in.get("TRNO") 
     } else {
        inTRNO = 0    
     }

     // Validate Deck Profile Detail record
     Optional<DBContainer> EXTDPD = findEXTDPD(inCONO, inDIVI, inDPID)
     if(!EXTDPD.isPresent()){
        mi.error("Deck Profile Details doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPD = EXTDPD.get() 
        detailLOAD = containerEXTDPD.get("EXLOAD") 
        detailDLOG = containerEXTDPD.get("EXDLOG") 
        detailGVBF = containerEXTDPD.get("EXGVBF") 
        detailNVBF = containerEXTDPD.get("EXNVBF") 
     } 

     // Validate deck register record
     Optional<DBContainer> EXTDPR = findEXTDPR(inCONO, inDIVI, inDPID, inTRNO)
     if(!EXTDPR.isPresent()){
        mi.error("Deck Register doesn't exist")   
        return             
     } else {
        // Record found, get info from register record
        DBContainer containerEXTDPR = EXTDPR.get() 
        inLOAD = containerEXTDPR.get("EXLOAD") 
        inTLOG = containerEXTDPR.get("EXTLOG") 
        inTGBF = containerEXTDPR.get("EXTGBF") 
        inTNBF = containerEXTDPR.get("EXTNBF") 

        //Sum of fields
        double totalLOAD
        double totalDLOG
        double totalGVBF
        double totalNVBF
    
        totalLOAD = detailLOAD - inLOAD
        totalDLOG = detailDLOG - inTLOG
        totalGVBF = detailGVBF - inTGBF
        totalNVBF = detailNVBF - inTNBF

        String inCONOString = String.valueOf(inCONO)
        String inDPIDString = String.valueOf(inDPID)
        String inLOADString = String.valueOf(totalLOAD)
        String inTLOGString = String.valueOf(totalDLOG)
        String inTGBFString = String.valueOf(totalGVBF)
        String inTNBFString = String.valueOf(totalNVBF)

        updDeckDetailsMI(inCONOString, inDIVI, inDPIDString, inLOADString, inTLOGString, inTGBFString, inTNBFString)   

        // Delete record 
        deleteEXTDPRRecord(inCONO, inDIVI, inDPID, inTRNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDPR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPR(int CONO, String DIVI, int DPID, int TRNO){  
     DBAction query = database.table("EXTDPR").index("00").build()
     DBContainer EXTDPR = query.getContainer()
     EXTDPR.set("EXCONO", CONO)
     EXTDPR.set("EXDIVI", DIVI)
     EXTDPR.set("EXDPID", DPID)
     EXTDPR.set("EXTRNO", TRNO)
     if(query.read(EXTDPR))  { 
       return Optional.of(EXTDPR)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDPR
  //******************************************************************** 
  void deleteEXTDPRRecord(int CONO, String DIVI, int DPID, int TRNO){ 
     DBAction action = database.table("EXTDPR").index("00").build()
     DBContainer EXTDPR = action.getContainer()
     EXTDPR.set("EXCONO", CONO)
     EXTDPR.set("EXDIVI", DIVI)
     EXTDPR.set("EXDPID", DPID)
     EXTDPR.set("EXTRNO", TRNO)

     action.readLock(EXTDPR, deleterCallbackEXTDPR)
  }
    
  Closure<?> deleterCallbackEXTDPR = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Get EXTDPD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID) {  
      DBAction query = database.table("EXTDPD").index("00").selection("EXLOAD", "EXDLOG", "EXGVBF", "EXNVBF").build()
      DBContainer EXTDPD = query.getContainer()
      EXTDPD.set("EXCONO", CONO)
      EXTDPD.set("EXDIVI", DIVI)
      EXTDPD.set("EXDPID", DPID)
      if(query.read(EXTDPD))  { 
        return Optional.of(EXTDPD)
      } 
  
      return Optional.empty()
   }
  
  
  //***************************************************************************** 
  // Update Deck Details
  //***************************************************************************** 
  void updDeckDetailsMI(String company, String division, String deckID, String load, String log, String grossVolume, String netVolume){   
      Map<String, String> params = [CONO: company, DIVI: division, DPID: deckID, LOAD: load, DLOG: log, GVBF: grossVolume, NVBF: netVolume] 
      Closure<?> callback = {
        Map<String, String> response ->
        if(response.DSID != null){
        }
      }

      miCaller.call("EXT005MI","UpdDeckDet", params, callback)
   } 


 }