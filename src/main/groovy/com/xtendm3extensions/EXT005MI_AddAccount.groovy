// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add account to EXTACT
// Transaction AddAccount
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: ACCD - Account Code
 * @param: NAME - Name
 * 
*/



public class AddAccount extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI

  
  // Constructor 
  public AddAccount(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
     inDIVI = mi.inData.get("DIVI").trim()
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Account Code
     String inACCD
     if (mi.inData.get("ACCD") != null) {
        inACCD = mi.inData.get("ACCD").trim() 
     } else {
        inACCD = ""         
     }
           
     // Name
     String inNAME 
     if (mi.inData.get("NAME") != null) {
        inNAME = mi.inData.get("NAME").trim() 
     } else {
        inNAME= ""      
     }


     // Validate account record
     Optional<DBContainer> EXTACT = findEXTACT(inCONO, inDIVI, inACCD)
     if(EXTACT.isPresent()){
        mi.error("Account already exists")   
        return             
     } else {
        // Write record 
        addEXTACTRecord(inCONO, inDIVI, inACCD, inNAME)          
     }  

  }
  
  

  //******************************************************************** 
  // Get EXTACT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTACT(int CONO, String DIVI, String ACCD){  
     DBAction query = database.table("EXTACT").index("00").build()
     DBContainer EXTACT = query.getContainer()
     EXTACT.set("EXCONO", CONO)
     EXTACT.set("EXDIVI", DIVI)
     EXTACT.set("EXACCD", ACCD)
     if(query.read(EXTACT))  { 
       return Optional.of(EXTACT)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTACT record 
  //********************************************************************     
  void addEXTACTRecord(int CONO, String DIVI, String ACCD, String NAME){     
       DBAction action = database.table("EXTACT").index("00").build()
       DBContainer EXTACT = action.createContainer()
       EXTACT.set("EXCONO", CONO)
       EXTACT.set("EXDIVI", DIVI)
       EXTACT.set("EXACCD", ACCD)
       EXTACT.set("EXNAME", NAME)   
       EXTACT.set("EXCHID", program.getUser())
       EXTACT.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTACT.set("EXRGDT", regdate) 
       EXTACT.set("EXLMDT", regdate) 
       EXTACT.set("EXRGTM", regtime)
       action.insert(EXTACT)         
 } 

     
} 

