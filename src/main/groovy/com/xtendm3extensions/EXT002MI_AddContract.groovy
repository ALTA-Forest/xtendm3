// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API will add a new contract to EXTCTH and EXTCTD
// Transaction AddContract
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTYP - Contract Type
 * @param: SUNO - Supplier
 * @param: CTMG - Contract Manager
 * @param: DLTY - Deliver to Yard
 * @param: ISTP - Template
 * @param: CFI5 - Payee Role
 * @param: CTTI - Contract Title
 * @param: VALF - Valid From
 * @param: VALT - Valid To
 * @param: STAT - Revision Status
 * @param: SUNM - Supplier Name
 * @param: FRSC - Frequency Scaling
 * @param: TPNO - Contract Template Number
*/

/**
 * OUT
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: CTNO - Contract Number
 * @param: RVNO - Current Revision Number
 * 
*/


public class AddContract extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  // Definition 
  Integer inCONO
  String inDIVI
  int outCTNO 
  int inCTNO
  int outRVNO
  int inRVNO
  String outRVID
  String nextNumber

  
  // Constructor 
  public AddContract(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Contract Type
     int inCTYP
     if (mi.in.get("CTYP") != null) {
        inCTYP = mi.in.get("CTYP") 
     } else {
        inCTYP = 0          
     }
      
     // Supplier
     String inSUNO
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
     String inCTMG  
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
     String inDLTY 
     if (mi.in.get("DLTY") != null && mi.in.get("DLTY") != "") {
        inDLTY = mi.inData.get("DLTY").trim() 
     } else {
        inDLTY = ""        
     }
     
     // Template
     int inISTP  
     if (mi.in.get("ISTP") != null) {
        inISTP = mi.in.get("ISTP") 
     } else {
        inISTP = 0          
     }

     // Payee Role
     int inCFI5
     if (mi.in.get("CFI5") != null) {
        inCFI5 = mi.in.get("CFI5") 
     } else {
        inCFI5 = 0          
     }
     
     // Contract Title
     String inCTTI
     if (mi.in.get("CTTI") != null && mi.in.get("CTTI") != "") {
        inCTTI = mi.inData.get("CTTI").trim() 
     } else {
        inCTTI = ""        
     }
          
     // Valid From
     int inVALF
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
     int inVALT
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
     
     if (inVALF > inVALT){
        mi.error("From Date must be before the To Date")   
        return             
     }
     
     // Status
     int inSTAT 
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } else {
        inSTAT = 0          
     }
     
     // Supplier Name
     String inSUNM
     if (mi.in.get("SUNM") != null && mi.in.get("SUNM") != "") {
        inSUNM = mi.inData.get("SUNM").trim() 
     } else {
        inSUNM = ""        
     }
     
     // Frequency Scaling
     int inFRSC 
     if (mi.in.get("FRSC") != null) {
        inFRSC = mi.in.get("FRSC") 
     }

     // Contract Template Number
     int inTPNO 
     if (mi.in.get("TPNO") != null) {
        inTPNO = mi.in.get("TPNO") 
     } else {
        inTPNO = 0          
     }

     String inCTYPString = String.valueOf(inCTYP)
     String inCFI5String = String.valueOf(inCFI5)
     String inVALFString = String.valueOf(inVALF)
     String inVALTString = String.valueOf(inVALT)
     String inFRSCString = String.valueOf(inFRSC)
     String inTPNOString = String.valueOf(inTPNO)

     //If Contract Template Number is entered, copy the template to create a new contract
     if (inTPNO != null && inTPNO > 0) {  
        
        // Validate if template contract header exists
        Optional<DBContainer> EXTCTH = findEXTCTH(inCONO, inDIVI, inTPNO)
        if(!EXTCTH.isPresent()){
           mi.error("Contract Template Number doesn't exists")   
           return             
        } 

        cpyContractTemplateMI(String.valueOf(inCONO), inDIVI, inCTYPString, inSUNO, inCTMG, inDLTY, inCFI5String, inCTTI, inVALFString, inVALTString, inSUNM, inFRSCString, inTPNOString)               

     } else {
       
       // Revision Number
       inRVNO = 1
       outRVNO = 1
       
       //Get next number for contract number
       getNextNumber("", "L1", "1") 
       outCTNO = nextNumber as Integer
       inCTNO = outCTNO as Integer
       
       //Revision ID
       String inRVID = String.valueOf(inCTNO) + String.valueOf(inRVNO)
       outRVID = inRVID
       
       // Validate contract header
       Optional<DBContainer> EXTCTH = findEXTCTH(inCONO, inDIVI, inCTNO)
       if(EXTCTH.isPresent()){
          mi.error("Contract Number already exists in Contract Header table")   
          return             
       } else {
          // Write record 
          addEXTCTHRecord(inCONO, inDIVI, inCTNO, inCTYP, inSUNO, inCTMG, inDLTY, inISTP, inCFI5, inCTTI, inVALF, inVALT, inSTAT, inSUNM, outRVID)  
          addEXTCTDRecord(inCONO, inDIVI, inCTNO, outRVID, inVALF, inVALT, inFRSC)          
       }
     }
     
     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("CTNO", String.valueOf(inCTNO)) 
     mi.outData.put("RVID", outRVID) 
     mi.outData.put("RVNO", "1")      
     mi.write()
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

  
   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 


   //***************************************************************************** 
   // Copy a template contract to create a new contract
   //***************************************************************************** 
   void cpyContractTemplateMI(String company, String division, String contractType, String supplier, String contractManager, String deliverToYard, String payeeRole, String contractTitle, String fromDate, String toDate, String supplierName, String frequencyScaling, String contractNumber){   
        Map<String, String> params = [CONO: company, DIVI: division, CTYP: contractType, SUNO: supplier, CTMG: contractManager, DLTY: deliverToYard, CFI5: payeeRole, CTTI: contractTitle, VALF: fromDate, VALT: toDate, SUNM: supplierName, FRSC: frequencyScaling, CTNO: contractNumber] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.CTNO != null){
             String revisionIDFromTemplate = response.RVID 
             String contractNumberFromTemplate = response.CTNO
             String revisionNumberFromTemplate = response.RVNO
             outRVID = revisionIDFromTemplate
             inCTNO = contractNumberFromTemplate as Integer
             outRVNO = 1
          }
        }
               
        miCaller.call("EXT002MI","CpyContrTemp", params, callback)        
   } 


  //******************************************************************** 
  // Add EXTCTH record
  //********************************************************************     
  void addEXTCTHRecord(int CONO, String DIVI, int CTNO, int CTYP, String SUNO, String CTMG, String DLTY, int ISTP, int CFI5, String CTTI, int VALF, int VALT, int STAT, String SUNM, String RVID){     
       DBAction action = database.table("EXTCTH").index("00").build()
       DBContainer EXTCTH = action.createContainer()
       EXTCTH.set("EXCONO", CONO)
       EXTCTH.set("EXDIVI", DIVI)
       EXTCTH.set("EXCTNO", CTNO)
       EXTCTH.set("EXCTYP", CTYP)
       EXTCTH.set("EXSUNO", SUNO)
       EXTCTH.set("EXCTMG", CTMG)
       EXTCTH.set("EXDLTY", DLTY)
       EXTCTH.set("EXISTP", ISTP)
       EXTCTH.set("EXCFI5", CFI5)
       EXTCTH.set("EXCTTI", CTTI)
       EXTCTH.set("EXSTAT", STAT)
       EXTCTH.set("EXSUNM", SUNM)
       EXTCTH.set("EXRVID", RVID)
       EXTCTH.set("EXRVNO", 1)   
       EXTCTH.set("EXCHID", program.getUser())
       EXTCTH.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTH.set("EXRGDT", regdate) 
       EXTCTH.set("EXLMDT", regdate) 
       EXTCTH.set("EXRGTM", regtime)
       action.insert(EXTCTH)         
 } 
 
 
  //******************************************************************** 
  // Add EXTCTD record 
  //********************************************************************     
  void addEXTCTDRecord(int CONO, String DIVI, int CTNO, String RVID, int VALF, int VALT, int FRSC){     
       DBAction action = database.table("EXTCTD").index("00").build()
       DBContainer EXTCTD = action.createContainer()
       EXTCTD.set("EXCONO", CONO)
       EXTCTD.set("EXDIVI", DIVI)
       EXTCTD.set("EXCTNO", CTNO)
       EXTCTD.set("EXRVNO", 1)
       EXTCTD.set("EXRVID", RVID)
       EXTCTD.set("EXVALF", VALF)
       EXTCTD.set("EXVALT", VALT)
       EXTCTD.set("EXSTAT", 10)
       EXTCTD.set("EXFRSC", FRSC)   
       EXTCTD.set("EXCHID", program.getUser())
       EXTCTD.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTD.set("EXRGDT", regdate) 
       EXTCTD.set("EXLMDT", regdate) 
       EXTCTD.set("EXRGTM", regtime)
       action.insert(EXTCTD)         
 } 
     
} 

