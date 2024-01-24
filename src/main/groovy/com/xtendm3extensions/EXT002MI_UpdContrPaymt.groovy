// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-25
// @version   1.0 
//
// Description 
// This API is to update a contract payment in EXTCPI
// Transaction UpdContrPaymt
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
 * 
*/



public class UpdContrPaymt extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inPINO
  String inRVID
  int inCTNO
  int inDLNO
  String inSUNO
  String inITNO
  int inPODT
  int inDUDT
  int inBADT
  String inPOTO  
  double inNEBF
  double inPIAM
  int inAPDT
  String inPIAU
  int inSTAT
  String inTREF
  
  // Constructor 
  public UpdContrPaymt(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger     
  } 
    
  public void main() {       
     // Set Company Number
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Payment Number
     if (mi.in.get("PINO") != null) {
        inPINO = mi.in.get("PINO") 
     } else {
        inPINO = 0        
     }

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0        
     }

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""         
     }

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
     }

     // Supplier
     if (mi.in.get("SUNO") != null) {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""         
     }

     // Item Number
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""         
     }
 
      // Date Posted
     if (mi.in.get("PODT") != null) {
        inPODT = mi.in.get("PODT") 
     } 

     // Due Date
     if (mi.in.get("DUDT") != null) {
        inDUDT = mi.in.get("DUDT") 
     } 

     // Batched Date
     if (mi.in.get("BADT") != null) {
        inBADT = mi.in.get("BADT") 
     }

     // Posted To
     if (mi.in.get("POTO") != null) {
        inPOTO = mi.inData.get("POTO").trim() 
     } else {
        inPOTO = ""         
     }

     // Net BF
     if (mi.in.get("NEBF") != null) {
        inNEBF = mi.in.get("NEBF") 
     } 
 
     // Amount
     if (mi.in.get("PIAM") != null) {
        inPIAM = mi.in.get("PIAM") 
     } 

     // Approve Date
     if (mi.in.get("APDT") != null) {
        inAPDT = mi.in.get("APDT") 
     }

     // Approver
     if (mi.in.get("PIAU") != null) {
        inPIAU = mi.inData.get("PIAU").trim() 
     } else {
        inPIAU = ""         
     }

     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } 

     // Reference
     if (mi.in.get("TREF") != null) {
        inTREF = mi.inData.get("TREF").trim() 
     } else {
        inTREF = ""         
     }
     

     // Validate contract payment record
     Optional<DBContainer> EXTCPI = findEXTCPI(inCONO, inDIVI, inPINO, inCTNO, inRVID, inDLNO)
     if(!EXTCPI.isPresent()){
        mi.error("Contract Payment doesn't exist")   
        return             
     }     
    
     // Update record
     updEXTCPIRecord()
     
  }
  
    
  //******************************************************************** 
  // Get EXTCPI record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCPI(int CONO, String DIVI, int PINO, int CTNO, String RVID, int DLNO){  
     DBAction query = database.table("EXTCPI").index("00").build()
     def EXTCPI = query.getContainer()
     EXTCPI.set("EXCONO", CONO)
     EXTCPI.set("EXDIVI", DIVI)
     EXTCPI.set("EXPINO", PINO)
     EXTCPI.set("EXCTNO", CTNO)
     EXTCPI.set("EXRVID", RVID)
     EXTCPI.set("EXDLNO", DLNO)
     
     if(query.read(EXTCPI))  { 
       return Optional.of(EXTCPI)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Update EXTCPI record
  //********************************************************************    
  void updEXTCPIRecord(){      
     DBAction action = database.table("EXTCPI").index("00").build()
     DBContainer EXTCPI = action.getContainer()     
     EXTCPI.set("EXCONO", inCONO)
     EXTCPI.set("EXDIVI", inDIVI)
     EXTCPI.set("EXPINO", inPINO)
     EXTCPI.set("EXCTNO", inCTNO)
     EXTCPI.set("EXRVID", inRVID)
     EXTCPI.set("EXDLNO", inDLNO)

     // Read with lock
     action.readLock(EXTCPI, updateCallBackEXTCPI)
     }
   
     Closure<?> updateCallBackEXTCPI = { LockedResult lockedResult -> 
       if (mi.in.get("SUNO") != null) {
          lockedResult.set("EXSUNO", mi.inData.get("SUNO").trim())
       }
  
       if (mi.in.get("ITNO") != null) {
          lockedResult.set("EXITNO", mi.inData.get("ITNO").trim())
       }
  
       if (mi.in.get("PODT") != null) {
          lockedResult.set("EXPODT", mi.in.get("PODT"))
       }
  
       if (mi.in.get("DUDT") != null) {
          lockedResult.set("EXDUDT", mi.in.get("DUDT"))
       }
  
       if (mi.in.get("BADT") != null) {
          lockedResult.set("EXBADT", mi.in.get("BADT"))
       }
  
       if (mi.in.get("POTO") != null) {
          lockedResult.set("EXPOTO", mi.inData.get("POTO").trim())
       }
  
       if (mi.in.get("NEBF") != null) {
          lockedResult.set("EXNEBF", mi.in.get("NEBF"))
       }
       
       if (mi.in.get("PIAM") != null) {
          lockedResult.set("EXPIAM", mi.in.get("PIAM"))
       }
       
       if (mi.in.get("APDT") != null) {
          lockedResult.set("EXAPDT", mi.in.get("APDT"))
       }
  
       if (mi.in.get("PIAU") != null) {
          lockedResult.set("EXPIAU", mi.inData.get("PIAU").trim())
       }
       if (mi.in.get("STAT") != null) {
          lockedResult.set("EXSTAT", mi.in.get("STAT"))
       }
  
       if (mi.in.get("TREF") != null) {
          lockedResult.set("EXTREF", mi.inData.get("TREF").trim())
       }

       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
  }

} 

