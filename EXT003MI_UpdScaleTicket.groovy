// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update scale ticket in EXTDST
// Transaction UpdScaleTicket
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STNO - Scale Ticket Number
 * @param: STDT - Scale Date
 * @param: STLR - Log Rule
 * @param: STLN - Scale Location Number
 * @param: STSN - Scaler Number
 * @param: STLP - Log Percentage
*/


public class UpdScaleTicket extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  String inSTNO
  int inSTDT
  int inSTLR
  String inSTLN
  String inSTSN
  int inSTLP
  
  // Constructor 
  public UpdScaleTicket(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
     }

     // Scale Ticket Number
     if (mi.in.get("STNO") != null) {
        inSTNO = mi.in.get("STNO") 
     } else {
        inSTNO = ""        
     }
           
     // Scale Date
     if (mi.in.get("STDT") != null) {
        inSTDT = mi.in.get("STDT") 
     } else {
        inSTDT = 0        
     }
 
      // Log Rule
     if (mi.in.get("STLR") != null) {
        inSTLR = mi.in.get("STLR") 
     } else {
        inSTLR = 0        
     }
     
     // Scale Location Number
     if (mi.in.get("STLN") != null) {
        inSTLN = mi.in.get("STLN") 
     } else {
        inSTLN = ""        
     }

     // Scaler Number
     if (mi.in.get("STSN") != null) {
        inSTSN = mi.in.get("STSN") 
     } else {
        inSTSN = ""        
     }

     // Log Percentage
     if (mi.in.get("STLP") != null) {
        inSTLP = mi.in.get("STLP") 
     } else {
        inSTLP = 0d       
     }


     // Validate Delivery Status record
     Optional<DBContainer> EXTDST = findEXTDST(inCONO, inDIVI, inDLNO, inSTNO)
     if (!EXTDST.isPresent()) {
        mi.error("Scale Ticket record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDSTRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTDST record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDST(int CONO, String DIVI, int DLNO, String STNO){  
     DBAction query = database.table("EXTDST").index("00").build()
     DBContainer EXTDST = query.getContainer()
     EXTDST.set("EXCONO", CONO)
     EXTDST.set("EXDIVI", DIVI)
     EXTDST.set("EXDLNO", DLNO)
     EXTDST.set("EXSTNO", STNO)
     if(query.read(EXTDST))  { 
       return Optional.of(EXTDST)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update EXTDST record
  //********************************************************************    
  void updEXTDSTRecord(){      
     DBAction action = database.table("EXTDST").index("00").build()
     DBContainer EXTDST = action.getContainer()     
     EXTDST.set("EXCONO", inCONO)
     EXTDST.set("EXDIVI", inDIVI)
     EXTDST.set("EXDLNO", inDLNO)
     EXTDST.set("EXSTNO", inSTNO)

     // Read with lock
     action.readLock(EXTDST, updateCallBackEXTDST)
     }
   
     Closure<?> updateCallBackEXTDST = { LockedResult lockedResult -> 
       if (inSTDT != 0) {
          lockedResult.set("EXSTDT", inSTDT)
       }
  
       if (inSTLR != 0) {
          lockedResult.set("EXSTLR", inSTLR)
       }
   
        if (inSTLN != "") {
          lockedResult.set("EXSTLN", inSTLN)
       }
  
       if (inSTSN != "") {
          lockedResult.set("EXSTSN", inSTSN)
       }
  
       if (inSTLP != 0d) {
          lockedResult.set("EXSTLP", inSTLP)
       }
      
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changedate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changedate)        
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

