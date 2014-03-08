package phonelibv2

import org.springframework.dao.DataIntegrityViolationException
import org.apache.shiro.SecurityUtils

import java.util.Iterator
/**
 * 用户拥有图书类
 * @author Administrator
 *
 */
class OwnController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def bgindex() {
        redirect(action: "bglist", params: params)
    }

    def bglist() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [ownInstanceList: Own.list(params), ownInstanceTotal: Own.count()]
    }

    def bgcreate() {
        [ownInstance: new Own(params)]
    }

    def bgsave() {
        def ownInstance = new Own(params)
        if (!ownInstance.save(flush: true)) {
            render(view: "bgcreate", model: [ownInstance: ownInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'own.label', default: 'Own'), ownInstance.id])
        redirect(action: "bgshow", id: ownInstance.id)
    }

    def bgshow() {
        def ownInstance = Own.get(params.id)
        if (!ownInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
            redirect(action: "bglist")
            return
        }

        [ownInstance: ownInstance]
    }

    def bgedit() {
        def ownInstance = Own.get(params.id)
        if (!ownInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
            redirect(action: "bglist")
            return
        }

        [ownInstance: ownInstance]
    }

    def bgupdate() {
        def ownInstance = Own.get(params.id)
        if (!ownInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
            redirect(action: "bglist")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (ownInstance.version > version) {
                ownInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'own.label', default: 'Own')] as Object[],
                          "Another user has updated this Own while you were editing")
                render(view: "bgedit", model: [ownInstance: ownInstance])
                return
            }
        }

        ownInstance.properties = params

        if (!ownInstance.save(flush: true)) {
            render(view: "bgedit", model: [ownInstance: ownInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'own.label', default: 'Own'), ownInstance.id])
        redirect(action: "bgshow", id: ownInstance.id)
    }

    def bgdelete() {
        def ownInstance = Own.get(params.id)
        if (!ownInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
            redirect(action: "bglist")
            return
        }

        try {
            ownInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'own.label', default: 'Own'), params.id])
            redirect(action: "bglist")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'own.label', default: 'Own'), params.id])
            redirect(action: "bgshow", id: params.id)
        }
    }
	
	
	def index() {
		redirect(action: "list", params: params)
	}
	
	def list() {
		
		def principal = SecurityUtils.subject?.principal
		def userInstance=ShiroUser.findByUsername(principal)
		
		List categoryList =	Category.list()
		List resquestArray = []
		categoryList.each{
			def c = Own.createCriteria()
			String cname = it.cname
			def searchByCategory = {
				book{
					category{
						eq('cname',cname)
					}
				}
				user{
					eq('username',userInstance.username)
				}
			}
			def ownBook = c.list(params,searchByCategory)
			print(ownBook)
			int bookCount = ownBook.totalCount
			resquestArray.add(categoryIntance:it,bookCount:bookCount)
		}
		resquestArray.bookCount.sort()
		categoryList = []
		resquestArray.each{
			if(it.bookCount != 0){
				categoryList.add(it)
			}
		}
		print categoryList
		
		
		
		def categoryInstance = Category.get(params.id)
		
		params.max = Math.min(params.max ? params.int('max') : 15, 100)
		def searchOwnList = {
			if(categoryInstance)
			book{
				category{
					eq('cname',categoryInstance.cname)
				}
			}
			user{
				eq('username',userInstance.username)
				
			}
		}
		def c = Own.createCriteria()
		def ownList = c.list(params,searchOwnList)
		def ownCount = ownList.totalCount
		def touxiangUrl = "touxiang/default_avatar.jpg"  //默认头像
		if(userInstance.btouxiang){//登录后，判断是否有头像
			def tSize = "btouxiang" //选择头像的类型，这里是大头像
			def tIndex = userInstance."${tSize}"?.indexOf("touxiang") 
			def touxiang =  userInstance."${tSize}"?.substring(tIndex)
			touxiangUrl = touxiang?.replace('\\', '/');            
        }
		
		[ownInstanceList: ownList, ownInstanceTotal: ownCount,categoryInstanceList: categoryList,shiroUserInstance:touxiangUrl]
			
		
		}
		
		def create() {
			[ownInstance: new Own(params)]
		}
	
		
		def save(){
			def book=Book.findByIsbn13(params.isbn)
			if(book==null){
				book = new Book()
				book.title = params.book_name
				book.isbn13 = params.isbn
				def cname = params.category.id
				def category = Category.findByCname(cname)
				category.addToBooks(book);
				if (!book.save(flush: true)) {
					render(view: "create", model: [book: Book])
					return
				}
			}
		
			def principal = SecurityUtils.subject?.principal
			def user=ShiroUser.findByUsername(principal)
			def now =new Date()
			def own =new Own(book:book,user:user,dateCreated:now)
			own.save()
			
			if(own.hasErrors()){
				println own.errors
				}
	
			flash.message = message(code: 'default.created.message', args: [message(code: 'own.label', default: 'Own'), book.id])
			redirect(action: "show", id: book.id)
		}
	
		def show() {
			def ownInstance = Own.get(params.id)
			if (!ownInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
				redirect(action: "list")
				return
			}
	
			[ownInstance: ownInstance]
		}
	
		def edit() {
			def ownInstance = Own.get(params.id)
			if (!ownInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
				redirect(action: "list")
				return
			}
	
			[ownInstance: ownInstance]
		}
	
		def update() {
			def ownInstance = Own.get(params.id)
			if (!ownInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
				redirect(action: "list")
				return
			}
	
			if (params.version) {
				def version = params.version.toLong()
				if (ownInstance.version > version) {
					ownInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
							  [message(code: 'own.label', default: 'Own')] as Object[],
							  "Another user has updated this Own while you were editing")
					render(view: "edit", model: [ownInstance: ownInstance])
					return
				}
			}
	
			ownInstance.properties = params
	
			if (!ownInstance.save(flush: true)) {
				render(view: "edit", model: [ownInstance: ownInstance])
				return
			}
	
			flash.message = message(code: 'default.updated.message', args: [message(code: 'own.label', default: 'Own'), ownInstance.id])
			redirect(action: "show", id: ownInstance.id)
		}
	
		def delete() {
			def ownInstance = Own.get(params.id)
			if (!ownInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'own.label', default: 'Own'), params.id])
				redirect(action: "list")
				return
			}
	
			try {
				ownInstance.delete(flush: true)
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'own.label', default: 'Own'), params.id])
				
				def userInstance = ownInstance.book.own.user
				println(userInstance)
				if(userInstance.isEmpty()){
					ownInstance.book.delete();
					println(userInstance)
				}
				
				redirect(action: "list")
			}
			catch (DataIntegrityViolationException e) {
				flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'own.label', default: 'Own'), params.id])
				redirect(action: "show", id: params.id)
			}
		}
		// phone 
		
		
		
		def ownbooklist(){
			def principal = SecurityUtils.subject?.principal
			def userInstance=ShiroUser.findByUsername(principal)

			params.max = Math.min(params.max ? params.int('max') : 15, 100)
			def ownInstance = userInstance.own
			List categoryList = ownInstance.book.category
			categoryList.unique()
			def categoryInstance = Category.get(params.id)
			def searchOwnList = {
				if(categoryInstance)
				book{
					category{
						eq('cname',categoryInstance.cname)
					}
				}
				user{
					eq('username',userInstance.username)
					
				}
			}
			def c = Own.createCriteria()
			def ownList = c.list(params,searchOwnList)
			def ownCount = ownList.totalCount

			
			render(contentType:"text/json"){
				ownInstanceList: ownList
				 ownInstanceTotal: ownCount
				 categoryInstanceList: categoryList
			}
		}
		
		def phoneSave(){
			def book=Book.findByIsbn13(params.isbn)
			if(book==null){
				book = new Book()
				book.title = params.book_name
				book.isbn13 = params.isbn
				def cname = params.category.id
				def category = Category.findByCname(cname)
				category.addToBooks(book);
				if (!book.save(flush: true)) {
					render(view: "create", model: [book: Book])
					return
				}
			}
		
			def principal = SecurityUtils.subject?.principal
			
			def user=ShiroUser.findByUsername(principal)
			def now =new Date()
			def own =new Own(book:book,user:user,dateCreated:now)
			own.save()
			
			if(own.hasErrors()){
				println own.errors
				}
	
			flash.message = message(code: 'default.created.message', args: [message(code: 'own.label', default: 'Own'), book.id])
			redirect(action: "show", id: book.id)
		}
		
	}
	
	
