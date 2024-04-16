// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update contract header in EXTCTH 
// Transaction UpdContract
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division Number
 * @param: CTYP - Contract Type
 * @param: SUNO - Supplier Number
 * @param: CTNO - Contract Number
 * @param: CTMG - Contract Manager
 * @param: DLTY - Deliver To Yard
 * @param: CTTI - Contract Title
 * @param: ISTP - Template
 * @param: CFI5 - Payee Role
 * @param: VALF - Valid From
 * @param: VALT - Valid To
 * @param: STAT - Status
 * @param: SUNM - Supplier Name
 * 
*/



public class UpdContract extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inCTNO
  int inCTYP
  String inSUNO
  String inCTMG
  String inDLTY
  int inISTP
  int inCFI5
  String inCTTI
  int inVALF
  int inVALT
  int inSTAT
  String inSUNM
  int inRVNO
  String inRVID
  
  // Constructor 
  public UpdContract(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0      
     }

     // Contract Type
     if (mi.in.get("CTYP") != null) {
        inCTYP = mi.in.get("CTYP") 
     }

     // Supplier
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
        
        // Validate supplier if entered
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inSUNO)
        if (!CIDMAS.isPresent()) {
           mi.error("Supplier doesn't exist")   
           return             
        }

     } else {
        inSUNO = ""        
     }
     
     // Contract Manager
     if (mi.in.get("CTMG") != null && mi.in.get("CTMG") != "") {
        inCTMG = mi.inData.get("CTMG").trim()
        
        // Validate user if entered
        Optional<DBContainer> CMNUSR = findCMNUSR(inCONO, inCTMG)
        if (!CMNUSR.isPresent()) {
           mi.error("Contract Manager doesn't exist")   
           return             
        }

     } else {
        inCTMG = ""        
     }
     
     // Deliver To Yard
     if (mi.in.get("DLTY") != null && mi.in.get("DLTY") != "") {
        inDLTY = mi.inData.get("DLTY").trim() 
     } else {
        inDLTY = ""        
     }
     
     // Template
     if (mi.in.get("ISTP") != null) {
        inISTP = mi.in.get("ISTP") 
     } 

     // Payee Role
     if (mi.in.get("CFI5") != null) {
        inCFI5 = mi.in.get("CFI5") 
     }
     
     // Contract Title
     if (mi.in.get("CTTI") != null && mi.in.get("CTTI") != "") {
        inCTTI = mi.inData.get("CTTI").trim() 
     } else {
        inCTTI = ""        
     }
     
     // Valid From
     if (mi.in.get("VALF") != null) {
        inVALF = mi.in.get("VALF") 
        
        //Validate date format
        boolean validVALF = utility.call("DateUtil", "isDateValid", String.valueOf(inVALF), "yyyyMMdd")  
        if (!validVALF) {
           mi.error("Valid From Date is not valid")   
           return  
        } 

     } else {
        inVALF = 0      
     }
 
     // Valid To
     if (mi.in.get("VALT") != null) {
        inVALT = mi.in.get("VALT") 
        
        //Validate date format
        boolean validVALT = utility.call("DateUtil", "isDateValid", String.valueOf(inVALT), "yyyyMMdd")  
        if (!validVALT) {
           mi.error("Valid To Date is not valid")   
           return  
        } 

     } else {
        inVALT = 0      
     }

     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } 
  
     // Supplier Name
     if (mi.in.get("SUNM") != null) {
        inSUNM = mi.in.get("SUNM") 
     } else {
        inSUNM = ""        
     }
     
     // Revision Number
     if (mi.in.get("RVNO") != null) {
        inRVNO = mi.in.get("RVNO") 
     }
  
     // Revision ID
     if (mi.in.get("RVID") != null && mi.in.get("RVID") != "") {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""        
     }
     
 
     // Validate contract header
     Optional<DBContainer> EXTCTH = findEXTCTH(inCONO, inDIVI, inCTNO)
     if(!EXTCTH.isPresent()){
        mi.error("Contract Number doesn't exist in Contract Header table")   
        return             
     }     
    
     // Write record
     updEXTCTHRecord()
     
  }
  
  //******************************************************************** 
  // Get EXTCTH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTH(int CONO, String DIVI, int CTNO){  
     DBAction query = database.table("EXTCTH").index("00").build()
     DBContainer EXTCTH = query.getContainer()
     EXTCTH.set("EXCONO", CONO)
     EXTCTH.set("EXDIVI", DIVI)
     EXTCTH.set("EXCTNO", CTNO)
     if(query.read(EXTCTH))  { 
       return Optional.of(EXTCTH)
     } 
  
     return Optional.empty()
  }
  
  
   //******************************************************************** 
   // Check Supplier
   //******************************************************************** 
   private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").build()   
     DBContainer CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
    
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
   }
   
   
   //******************************************************************** 
   // Check User
   //******************************************************************** 
   private Optional<DBContainer> findCMNUSR(int CONO, String USID){  
     DBAction query = database.table("CMNUSR").index("00").build()   
     DBContainer CMNUSR = query.getContainer()
     CMNUSR.set("JUCONO", 0)
     CMNUSR.set("JUDIVI", "")
     CMNUSR.set("JUUSID", USID)
    
     if(query.read(CMNUSR))  { 
       return Optional.of(CMNUSR)
     } 
  
     return Optional.empty()
   }


  //******************************************************************** 
  // Update EXTCTH record
  //********************************************************************    
  void updEXTCTHRecord(){      
     DBAction action = database.table("EXTCTH").index("00").build()
     DBContainer EXTCTH = action.getContainer()    
     EXTCTH.set("EXCONO", inCONO)     
     EXTCTH.set("EXDIVI", inDIVI)  
     EXTCTH.set("EXCTNO", inCTNO)

     // Read with lock
     action.readLock(EXTCTH, updateCallBackEXTCTH)
     }
   
     Closure<?> updateCallBackEXTCTH = { LockedResult lockedResult -> 
       if (mi.in.get("CTYP") != null) {
          lockedResult.set("EXCTYP", mi.in.get("CTYP"))
       }
       
       if (inSUNO != "") {
          lockedResult.set("EXSUNO", inSUNO)
       }
  
       if (inCTMG != "") {
          lockedResult.set("EXCTMG", inCTMG)
       }
  
       if (inDLTY != "") {
          lockedResult.set("EXDLTY", inDLTY)
       }
  
       if (mi.in.get("ISTP") != null) {
          lockedResult.set("EXISTP", mi.in.get("ISTP"))
       }
       
       if (mi.in.get("CFI5") != null) {
          lockedResult.set("EXCFI5", mi.in.get("CFI5"))
       }
       
       if (inCTTI != "") {
          lockedResult.set("EXCTTI", inCTTI)
       }
  
       if (inVALF != 0) {
          lockedResult.set("EXVALF", inVALF)
       }
  
       if (inVALT != 0) {
          lockedResult.set("EXVALT", inVALT)
       }
  
       if (mi.in.get("STAT") != null) {
          lockedResult.set("EXSTAT", mi.in.get("STAT"))
       }
  
       if (inSUNM != "") {
          lockedResult.set("EXSUNM", inSUNM)
       }
  
       if (mi.in.get("RVNO") != null) {
          lockedResult.set("EXRVNO", mi.in.get("RVNO"))
       }
  
       if (inRVID != "") {
          lockedResult.set("EXRVID", inRVID)
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

