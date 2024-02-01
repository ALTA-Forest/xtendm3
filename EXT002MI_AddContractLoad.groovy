// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-07-06
// @version   1.0 
//
// Description 
// This API is to add contract load to EXTCTL
// Transaction AddContractLoad
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * @param: TNLB - Total Net lbs
 * @param: TNBF - Total Net bf
 * @param: AVBL - Average bf/lbs
 * @param: AMNT - Amount
 * 
*/


public class AddContractLoad extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  
  // Constructor 
  public AddContractLoad(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Delivery Number
     int inDLNO  
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
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
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""         
     }

     // Total Net lbs
     double inTNLB  
     if (mi.in.get("TNLB") != null) {
        inTNLB = mi.in.get("TNLB") 
     } else {
        inTNLB = 0d        
     }
     
     // Total Net bf
     double inTNBF  
     if (mi.in.get("TNBF") != null) {
        inTNBF = mi.in.get("TNBF") 
     } else {
        inTNBF = 0d        
     }
      
     // Average lbs/bf
     double inAVBL  
     if (mi.in.get("AVBL") != null) {
        inAVBL = mi.in.get("AVBL") 
     } else {
        inAVBL = 0d        
     }
     
     // Amount
     double inAMNT  
     if (mi.in.get("AMNT") != null) {
        inAMNT = mi.in.get("AMNT") 
     } else {
        inAMNT = 0d        
     }
 

     // Validate Contract Load record
     Optional<DBContainer> EXTCTL = findEXTCTL(inCONO, inDIVI, inDLNO, inCTNO, inRVID)
     if(EXTCTL.isPresent()){
        mi.error("Contract Load already exists")   
        return             
     } else {
        // Write record 
        addEXTCTLRecord(inCONO, inDIVI, inDLNO, inCTNO, inRVID, inTNLB, inTNBF, inAVBL, inAMNT)          
     }  

  }
  


  //******************************************************************** 
  // Get EXTCTL record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTL(int CONO, String DIVI, int DLNO, int CTNO, String RVID){  
     DBAction query = database.table("EXTCTL").index("00").build()
     def EXTCTL = query.getContainer()
     EXTCTL.set("EXCONO", CONO)
     EXTCTL.set("EXDIVI", DIVI)
     EXTCTL.set("EXDLNO", DLNO)
     EXTCTL.set("EXCTNO", CTNO)
     EXTCTL.set("EXRVID", RVID)
     if(query.read(EXTCTL))  { 
       return Optional.of(EXTCTL)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCTL record 
  //********************************************************************     
  void addEXTCTLRecord(int CONO, String DIVI, int DLNO, int CTNO, String RVID, double TNLB, double TNBF, double AVBL, double AMNT){     
       DBAction action = database.table("EXTCTL").index("00").build()
       DBContainer EXTCTL = action.createContainer()
       EXTCTL.set("EXCONO", CONO)
       EXTCTL.set("EXDIVI", DIVI)
       EXTCTL.set("EXDLNO", DLNO)
       EXTCTL.set("EXCTNO", CTNO)
       EXTCTL.set("EXRVID", RVID)
       EXTCTL.set("EXTNLB", TNLB)
       EXTCTL.set("EXTNBF", TNBF)
       EXTCTL.set("EXAVBL", AVBL)
       EXTCTL.set("EXAMNT", AMNT)   
       EXTCTL.set("EXCHID", program.getUser())
       EXTCTL.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTL.set("EXRGDT", regdate) 
       EXTCTL.set("EXLMDT", regdate) 
       EXTCTL.set("EXRGTM", regtime)
       action.insert(EXTCTL)         
 } 

     
} 

