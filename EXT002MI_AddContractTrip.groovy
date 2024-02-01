// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-07-06
// @version   1.0 
//
// Description 
// This API is to add contract load to EXTCTT
// Transaction AddContractTrip
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * @param: DLFY - Deliver From Yard
 * @param: DLTY - Deliver To Yard
 * @param: TRRA - Trip Rate
 * @param: MTRA - Minimum Amount
*/


public class AddContractTrip extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  
  // Constructor 
  public AddContractTrip(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger
     this.utility = utility
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

     // Contract Number
     int inCTNO  
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0        
     }

     // Revision ID
     String inRVID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }

     // Deliver From Yard
     String inDLFY
     if (mi.in.get("DLFY") != null) {
        inDLFY = mi.in.get("DLFY") 
     } else {
        inDLFY = ""         
     }
     
     // Deliver To Yard
     String inDLTY
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.in.get("DLTY") 
     } else {
        inDLTY = ""         
     }
 
     // Trip Rate
     double inTRRA  
     if (mi.in.get("TRRA") != null) {
        inTRRA = mi.in.get("TRRA") 
     } else {
        inTRRA = 0d        
     }
 
     // Minimum Amount
     double inMTRA  
     if (mi.in.get("MTRA") != null) {
        inMTRA = mi.in.get("MTRA") 
     } else {
        inMTRA = 0d        
     }


     // Validate Contract Load record
     Optional<DBContainer> EXTCTT = findEXTCTT(inCONO, inDIVI, inCTNO, inRVID, inDLFY, inDLTY)
     if(EXTCTT.isPresent()){
        mi.error("Contract Trip already exists")   
        return             
     } else {
        // Write record 
        addEXTCTTRecord(inCONO, inDIVI, inCTNO, inRVID, inDLFY, inDLTY, inTRRA, inMTRA)          
     }  

  }
  


  //******************************************************************** 
  // Get EXTCTT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTT(int CONO, String DIVI, int CTNO, String RVID, String DLFY, String DLTY){  
     DBAction query = database.table("EXTCTT").index("00").build()
     def EXTCTT = query.getContainer()
     EXTCTT.set("EXCONO", CONO)
     EXTCTT.set("EXDIVI", DIVI)
     EXTCTT.set("EXCTNO", CTNO)
     EXTCTT.set("EXRVID", RVID)
     EXTCTT.set("EXDLFY", DLFY)
     EXTCTT.set("EXDLTY", DLTY)
     if(query.read(EXTCTT))  { 
       return Optional.of(EXTCTT)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCTT record 
  //********************************************************************     
  void addEXTCTTRecord(int CONO, String DIVI, int CTNO, String RVID, String DLFY, String DLTY, double TRRA, double MTRA){ 
       DBAction action = database.table("EXTCTT").index("00").build()
       DBContainer EXTCTT = action.createContainer()
       EXTCTT.set("EXCONO", CONO)
       EXTCTT.set("EXDIVI", DIVI)
       EXTCTT.set("EXCTNO", CTNO)
       EXTCTT.set("EXRVID", RVID)
       EXTCTT.set("EXDLFY", DLFY)
       EXTCTT.set("EXDLTY", DLTY)
       EXTCTT.set("EXTRRA", TRRA)
       EXTCTT.set("EXMTRA", MTRA)   
       EXTCTT.set("EXCHID", program.getUser())
       EXTCTT.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTT.set("EXLMDT", regdate) 
       EXTCTT.set("EXRGTM", regtime)
       action.insert(EXTCTT)         
 } 

     
} 

