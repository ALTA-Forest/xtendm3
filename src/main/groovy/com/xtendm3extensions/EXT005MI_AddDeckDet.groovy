// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add deck detail to EXTDPD
// Transaction AddDeckDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: LOAD - Current Loads
 * @param: DLOG - Current Logs
 * @param: GVBF - Gross Volume BF
 * @param: NVBF - Net Volume BF
 * @param: ESWT - Estimated Weight
 * @param: TCOI - Cost of Inventory
 * @param: AWBF - Average Weight of 1 Mbf
 * @param: GBFL - Average Gross bf/Log
 * @param: NBFL - Average Net bf/Log
 * 
*/



public class AddDeckDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDPID

  
  // Constructor 
  public AddDeckDet(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.utility = utility
     this.program = program
     this.logger = logger
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
           
     // Current Loads
     double inLOAD  
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } else {
        inLOAD= 0d      
     }

     // Current Logs
     double inDLOG 
     if (mi.in.get("DLOG") != null) {
        inDLOG = mi.in.get("DLOG") 
     } else {
        inDLOG = 0d        
     }

     // Gross Volume
     double inGVBF 
     if (mi.in.get("GVBF") != null) {
        inGVBF = mi.in.get("GVBF") 
     } else {
        inGVBF = 0d        
     }
     
     // Net Volume
     double inNVBF
     if (mi.in.get("NVBF") != null) {
        inNVBF = mi.in.get("NVBF") 
     } else {
        inNVBF = 0d        
     }

     // Estimated Weight
     double inESWT
     if (mi.in.get("ESWT") != null) {
        inESWT = mi.in.get("ESWT") 
     } else {
        inESWT = 0d        
     }

     // Cost of Inventory
     double inTCOI
     if (mi.in.get("TCOI") != null) {
        inTCOI = mi.in.get("TCOI") 
     } else {
        inTCOI = 0d        
     }

     // Average Weight of 1 Mbf
     double inAWBF
     if (mi.in.get("AWBF") != null) {
        inAWBF = mi.in.get("AWBF") 
     } else {
        inAWBF = 0d        
     }

     // Average Gross bf/Log
     double inGBFL
     if (mi.in.get("GBFL") != null) {
        inGBFL = mi.in.get("GBFL") 
     } else {
        inGBFL = 0d        
     }

     // Average Net bf/Log
     double inNBFL
     if (mi.in.get("NBFL") != null) {
        inNBFL = mi.in.get("NBFL") 
     } else {
        inNBFL = 0d        
     }


     // Validate Deck Profile Detail record
     Optional<DBContainer> EXTDPD = findEXTDPD(inCONO, inDIVI, inDPID)
     if(EXTDPD.isPresent()){
        mi.error("Deck Profile Details already exists")   
        return             
     } else {
        // Write record 
        addEXTDPDRecord(inCONO, inDIVI, inDPID, inLOAD, inDLOG, inGVBF, inNVBF, inESWT, inTCOI, inAWBF, inGBFL, inNBFL)          
     }  

  }
  
  

  //******************************************************************** 
  // Get EXTDPD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID){  
     DBAction query = database.table("EXTDPD").index("00").build()
     DBContainer EXTDPD = query.getContainer()
     EXTDPD.set("EXCONO", CONO)
     EXTDPD.set("EXDIVI", DIVI)
     EXTDPD.set("EXDPID", DPID)
     if(query.read(EXTDPD))  { 
       return Optional.of(EXTDPD)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTDPD record 
  //********************************************************************     
  void addEXTDPDRecord(int CONO, String DIVI, int DPID, double LOAD, double DLOG, double GVBF, double NVBF, double ESWT, double TCOI, double AWBF, double GBFL, double NBFL){     
       DBAction action = database.table("EXTDPD").index("00").build()
       DBContainer EXTDPD = action.createContainer()
       EXTDPD.set("EXCONO", CONO)
       EXTDPD.set("EXDIVI", DIVI)
       EXTDPD.set("EXDPID", DPID)
       EXTDPD.set("EXLOAD", LOAD)
       EXTDPD.set("EXDLOG", DLOG)
       EXTDPD.set("EXGVBF", GVBF)
       EXTDPD.set("EXNVBF", NVBF)
       EXTDPD.set("EXESWT", ESWT)
       EXTDPD.set("EXCOST", TCOI)
       EXTDPD.set("EXAWBF", AWBF)
       EXTDPD.set("EXGBFL", GBFL)
       EXTDPD.set("EXNBFL", NBFL)          
       EXTDPD.set("EXCHID", program.getUser())
       EXTDPD.set("EXCHNO", 1)           
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")          
       EXTDPD.set("EXRGDT", regdate) 
       EXTDPD.set("EXLMDT", regdate) 
       EXTDPD.set("EXRGTM", regtime)
       action.insert(EXTDPD)         
 } 

     
} 

